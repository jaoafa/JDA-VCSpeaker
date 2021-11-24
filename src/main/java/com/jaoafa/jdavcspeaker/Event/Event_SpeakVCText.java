package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.htmlparser.jericho.Source;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Event_SpeakVCText extends ListenerAdapter {
    final Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    final Pattern messageUrlPattern = Pattern.compile("^https://.*?discord\\.com/channels/([0-9]+)/([0-9]+)/([0-9]+)$", Pattern.CASE_INSENSITIVE);
    final Pattern tweetUrlPattern = Pattern.compile("^https://twitter\\.com/(\\w){1,15}/status/([0-9]+)$", Pattern.CASE_INSENSITIVE);
    final Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    final Pattern spoilerPattern = Pattern.compile("\\|\\|.+\\|\\|");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        final JDA jda = event.getJDA();
        final TextChannel channel = event.getTextChannel();
        final Message message = event.getMessage();
        if (channel.getIdLong() != MultipleServer.getVCChannelId(event.getGuild())) {
            return; // VCテキストチャンネル以外からのメッセージ
        }
        final Member member = event.getMember();
        if (member == null) {
            return;
        }

        User user = member.getUser();
        if (user.isBot()) {
            return;
        }

        final String content = message.getContentDisplay(); // チャンネル名・リプライとかが表示通りになっている文字列が返る
        if (content.equals(".")) {
            return; // .のみは除外
        }
        if (content.startsWith("!")) {
            return; // !から始まるコマンドと思われる文字列を除外
        }

        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            // 自身がどこにも入っていない場合

            if (member.getVoiceState() != null &&
                member.getVoiceState().getChannel() != null) {
                // メッセージ送信者がどこかのVCに入っている場合

                event.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel()); // 参加
                if (MultipleServer.getVCChannel(event.getGuild()) != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(":white_check_mark: AutoJoined")
                        .setDescription("`" + member.getVoiceState().getChannel().getName() + "`へ自動接続しました。")
                        .setColor(LibEmbedColor.success);
                    MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
                }
            } else {
                return; // 自身がどこにも入っておらず、送信者もどこにも入っていない場合
            }
        }

        // ignore
        boolean ignoreEquals = LibValue.ignoreEquals.contains(content);
        boolean ignoreContain = LibValue.ignoreContains.stream().anyMatch(content::contains);

        if (ignoreEquals || ignoreContain) return;

        // 読み上げるメッセージの構築
        String speakContent = content;

        // Replace url
        speakContent = replacerLink(jda, speakContent);
        // Spoiler
        speakContent = replacerSpoiler(speakContent);
        // Emphasize
        boolean isEmphasize = isEmphasizeMessage(speakContent);
        if (isEmphasize) {
            speakContent = replacerEmphasizeMessage(speakContent);
        }

        UserVoiceTextResult uvtr = getUserVoiceText(user);
        if (uvtr.isReset()) {
            message.reply("デフォルトパラメーターが不正であるため、リセットしました。").queue();
        }
        VoiceText vt = isEmphasize ? changeEmphasizeSpeed(uvtr.getVoiceText()) : uvtr.getVoiceText();
        vt.play(TrackInfo.SpeakFromType.RECEIVED_MESSAGE, message, speakContent);

        // 画像等
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
            if (attachment.isSpoiler()) {
                vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "スポイラーファイルが送信されました。");
                return;
            }
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

    String replacerLink(JDA jda, String content) {
        // messageUrlPattern.matcher(url);
        Matcher m = urlPattern.matcher(content);
        while (m.find()) {
            String url = m.group();

            // Discordメッセージリンク
            Matcher msgUrlMatcher = messageUrlPattern.matcher(url);
            if (msgUrlMatcher.find()) {
                String channelId = msgUrlMatcher.group(2);
                String messageId = msgUrlMatcher.group(3);
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) continue;
                Message message = channel.retrieveMessageById(messageId).complete();
                if (message == null) continue;

                String replaceTo = MessageFormat.format("{0}が{1}で送信したメッセージのリンク",
                    message.getAuthor().getAsTag(),
                    channel.getName());
                content = content.replace(url, replaceTo);
                continue;
            }

            Matcher tweetUrlMatcher = tweetUrlPattern.matcher(url);
            if (tweetUrlMatcher.find()) {
                String screenName = tweetUrlMatcher.group(1);
                String tweetId = tweetUrlMatcher.group(2);

                Tweet tweet = getTweet(screenName, tweetId);
                if (tweet != null) {
                    System.out.println(tweet);
                    String replaceTo = "%sのツイート「%s」へのリンク".formatted(
                        EmojiParser.removeAllEmojis(tweet.authorName()),
                        tweet.plainText()
                    );
                    content = content.replace(url, replaceTo);
                    continue;
                }
            }

            // GIFリンク
            if (url.endsWith(".gif")) {
                content = content.replace(url, "GIF画像へのリンク");
                continue;
            }

            // Webページのタイトル取得
            String title = getTitle(url);
            if (title != null) {
                System.out.println("title: " + title);
                if (title.length() >= 30) {
                    title = title.substring(0, 30) + "以下略";
                }
                System.out.println("title 2: " + title);
                content = content.replace(url, MessageFormat.format("Webページ「{0}」へのリンク", title));
            } else {
                content = content.replace(url, "Webページへのリンク");
            }
        }
        return content;
    }

    String replacerSpoiler(String content) {
        return spoilerPattern.matcher(content).replaceAll(" ピー ");
    }

    boolean isEmphasizeMessage(String content) {
        return
            content.matches("^\\*\\*(.[　 ]){2,}.\\*\\*$") || // **あ い う え お** OR **あ　い　う　え　お**
                content.matches("^(.[　 ]){2,}.$") || // あ い う え お OR あ　い　う　え　お
                content.matches("^\\*\\*(.+)\\*\\*$"); // **あああああああ**
    }

    String replacerEmphasizeMessage(String content) {
        for (String s : Arrays.asList("**", " ", "　")) {
            content = content.replace(s, "");
        }
        return content;
    }

    VoiceText changeEmphasizeSpeed(VoiceText vt) {
        try {
            return vt.setSpeed(Math.max(vt.getSpeed() / 2, 50));
        } catch (VoiceText.WrongSpeedException e) {
            return vt;
        }
    }

    @Nullable
    String getTitle(String url) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 200 && response.code() != 302) {
                    return null;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    return null;
                }
                Matcher m = titlePattern.matcher(body.string());
                return m.find() ? m.group(1) : null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    record Tweet(String authorName, String html, String plainText) {
    }

    @Nullable
    Tweet getTweet(String screenName, String tweetId) {
        String url = "https://publish.twitter.com/oembed?url=https://twitter.com/%s/status/%s".formatted(screenName, tweetId);
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 200 && response.code() != 302) {
                    return null;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    return null;
                }
                JSONObject json = new JSONObject(body.string());
                String html = json.getString("html");
                String authorName = json.getString("author_name");
                String plainText = new Source(html)
                    .getFirstElement("p")
                    .getRenderer()
                    .setMaxLineLength(Integer.MAX_VALUE)
                    .setNewLine(null)
                    .toString();
                return new Tweet(authorName, html, plainText);
            }
        } catch (IOException e) {
            return null;
        }
    }

    UserVoiceTextResult getUserVoiceText(User user) {
        try {
            return new UserVoiceTextResult(new VoiceText(user), false);
        } catch (VoiceText.WrongException e) {
            new DefaultParamsManager(user).setDefaultVoiceText(null);
            return new UserVoiceTextResult(new VoiceText(), true);
        }
    }

    record UserVoiceTextResult(VoiceText vt, boolean isReset) {
        public VoiceText getVoiceText() {
            return vt;
        }
    }
}
