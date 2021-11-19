package com.jaoafa.jdavcspeaker.Lib.MsgFormatter;

import com.jaoafa.jdavcspeaker.Lib.VisionAPI;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LibVisionAPI {
    public static void check(Message message, VoiceText vt) {
        VisionAPI visionAPI = Main.getVisionAPI();
        if (visionAPI == null) {
            message.getAttachments()
                .forEach(attachment -> vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "ファイル「" + attachment.getFileName() + "」が送信されました。"));
            return;
        }
        if (!new File("tmp").exists()) {
            boolean bool = new File("tmp").mkdirs();
            if (!bool) System.out.println("temporary folder was created.");
        }
        for (Message.Attachment attachment : message.getAttachments()) {
            attachment.downloadToFile("tmp/" + attachment.getFileName()).thenAcceptAsync(file -> {
                try {
                    List<VisionAPI.Result> results = visionAPI.getImageLabelOrText(file);
                    boolean bool = file.delete();
                    System.out.println("Temp attachment file have been delete " + (bool ? "successfully" : "failed"));
                    if (results == null) {
                        vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "ファイル「" + attachment.getFileName() + "」が送信されました。");
                        return;
                    }

                    List<VisionAPI.Result> sortedResults = results.stream()
                        .sorted(Comparator.comparing(VisionAPI.Result::getScore, Comparator.reverseOrder()))
                        .collect(Collectors.toList());
                    String descriptions = sortedResults.stream()
                        .map(VisionAPI.Result::getDescription)
                        .map(s -> s.length() > 15 ? s.substring(0, 15) : s)
                        .limit(3)
                        .collect(Collectors.joining("、"));
                    vt.play(TrackInfo.SpeakFromType.RECEIVED_IMAGE, message, "画像ファイル「" + descriptions + "を含む画像」が送信されました。");

                    String text = sortedResults.stream()
                        .filter(r -> r.getType() == VisionAPI.ResultType.TEXT_DETECTION)
                        .map(VisionAPI.Result::getDescription)
                        .findFirst()
                        .orElse(null);
                    String details = sortedResults.stream()
                        .filter(r -> r.getType() == VisionAPI.ResultType.LABEL_DETECTION)
                        .map(r -> String.format("`%s`%s",
                            r.getDescription(),
                            r.getDescription().equals(r.getRawDescription()) ? "" : " (`" + r.getRawDescription() + "`)"))
                        .collect(Collectors.joining("\n・"));
                    message.reply((text != null ? "```\n" + text.replaceAll("\n", " ") + "\n```" : "") + "・" + details).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
