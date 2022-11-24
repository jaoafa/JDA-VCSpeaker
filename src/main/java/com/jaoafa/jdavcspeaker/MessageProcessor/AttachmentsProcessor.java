package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * 添付ファイルのメッセージプロセッサ
 * <p>
 * ・拡張子で画像系ファイルであれば画像として扱う
 * 　・その後、MimeTypeでも判定する
 * ・拡張子で画像系ファイルでなければファイルとして扱う。この際はファイル名を読み上げる
 */
public class AttachmentsProcessor implements BaseProcessor {
    @Override
    public ProcessorType getType() {
        return ProcessorType.ATTACHMENTS;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        for (Message.Attachment attachment : message.getAttachments()) {
            // スポイラーファイルは無条件でスポイラーファイルとして読み上げ
            if (attachment.isSpoiler()) {
                uvtr.vt().play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "スポイラーファイルが送信されました。");
                return;
            }

            // 画像か、それ以外で分岐する
            if (VisionAPI.isCheckTarget(attachment.getFileExtension())) {
                processImage(message, uvtr.vt(), attachment);
                return;
            }

            processFile(message, uvtr.vt(), attachment);
        }
    }

    void processImage(Message message, VoiceText vt, Message.Attachment attachment) {
        VisionAPI visionAPI = Main.getVisionAPI();
        if (visionAPI == null) {
            processFile(message, vt, attachment);
            return;
        }
        LibFiles.VDirectory.VISION_API_TEMP.mkdirs();
        attachment
            .getProxy()
            .downloadToFile(LibFiles.VDirectory.VISION_API_TEMP.getPath().resolve(attachment.getFileName()).toFile())
            .thenAcceptAsync(file -> {
                try {
                    List<VisionAPI.Result> results = visionAPI.getImageLabelOrText(file);
                    boolean bool = file.delete();
                    LibFlow flow = new LibFlow("SpeakVCText.VisionAPI");
                    if (bool) {
                        flow.success("Temp attachment file have been delete successfully");
                    } else {
                        flow.success("Temp attachment file have been delete failed");
                    }
                    if (results == null) {
                        processFile(message, vt, attachment);
                        return;
                    }

                    List<VisionAPI.Result> sortedResults = results.stream()
                        .sorted(Comparator.comparing(VisionAPI.Result::getScore, Comparator.reverseOrder()))
                        .toList();
                    String text = sortedResults.stream()
                        .filter(r -> r.getType() == VisionAPI.ResultType.TEXT_DETECTION)
                        .map(VisionAPI.Result::getDescription)
                        .findFirst()
                        .orElse(null);

                    if (text != null) {
                        vt.play(TrackInfo.SpeakFromType.RECEIVED_IMAGE, message, "画像ファイル「%s を含む画像」が送信されました。".formatted(text.length() > 30 ? text.substring(0, 30) : text));

                        File outputFile = null;
                        try {
                            // 本来はこのタイミングでダウンロードが完了しているので、PHP側でもダウンロードするのではなくダウンロード済みファイルを利用するべき
                            // 気が向いたら修正
                            outputFile = LibTextImg.getTempimgPath(attachment.getUrl());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        MessageAction action;
                        EmbedBuilder embed = new EmbedBuilder()
                            .setDescription("```\n" + safeSubstring(text.replaceAll("\n", " ")) + "\n```");
                        if (outputFile != null) {
                            action = message.getChannel().sendFile(outputFile, "output.png");
                            embed = embed.setThumbnail("attachment://output.png");
                        } else {
                            action = message.getChannel().sendMessageEmbeds(embed.build());
                        }
                        action
                            .setEmbeds(embed.build())
                            .reference(message)
                            .mentionRepliedUser(false).queue();
                    } else {
                        vt.play(TrackInfo.SpeakFromType.RECEIVED_IMAGE, message, "画像ファイル「 %s 」が送信されました。".formatted(attachment.getFileName()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    void processFile(Message message, VoiceText vt, Message.Attachment attachment) {
        vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "ファイル「 %s 」が送信されました。".formatted(attachment.getFileName()));
    }

    String safeSubstring(String str) {
        if (str.length() <= 1500) {
            return str;
        }
        return str.substring(0, 1500 - 1);
    }
}
