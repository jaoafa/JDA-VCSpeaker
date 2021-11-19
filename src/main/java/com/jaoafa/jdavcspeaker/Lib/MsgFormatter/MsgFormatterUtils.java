package com.jaoafa.jdavcspeaker.Lib.MsgFormatter;

import com.jaoafa.jdavcspeaker.Lib.DefaultParamsManager;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.htmlparser.jericho.Source;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsgFormatterUtils {
    static final Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    static final Pattern spoilerPattern = Pattern.compile("\\|\\|.+\\|\\|");
    static final Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    static final Pattern messageUrlPattern = Pattern.compile("^https://.*?discord\\.com/channels/([0-9]+)/([0-9]+)/([0-9]+)$", Pattern.CASE_INSENSITIVE);
    static final Pattern tweetUrlPattern = Pattern.compile("^https://twitter\\.com/(\\w){1,15}/status/([0-9]+)$", Pattern.CASE_INSENSITIVE);


    public static String replacerSpoiler(String content) {
        return spoilerPattern.matcher(content).replaceAll(" ピー ");
    }

    public static String replacerLink(JDA jda, String content) {
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

    public static boolean isEmphasizeMessage(String content) {
        return
            content.matches("^\\*\\*(.[　 ]){2,}.\\*\\*$") || // **あ い う え お** OR **あ　い　う　え　お**
                content.matches("^(.[　 ]){2,}.$") || // あ い う え お OR あ　い　う　え　お
                content.matches("^\\*\\*(.+)\\*\\*$"); // **あああああああ**
    }

    public static String replacerEmphasizeMessage(String content) {
        for (String s : Arrays.asList("**", " ", "　"))
            content = content.replace(s, "");
        return content;
    }

    public static VoiceText changeEmphasizeSpeed(VoiceText vt) {
        try {
            return vt.setSpeed(Math.max(vt.getSpeed() / 2, 50));
        } catch (VoiceText.WrongSpeedException e) {
            return vt;
        }
    }

    @Nullable
    public static String getTitle(String url) {
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


    @Nullable
    public static Tweet getTweet(String screenName, String tweetId) {
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

    public static record Tweet(String authorName, String html, String plainText) {
    }

    public static UserVoiceTextResult getUserVoiceText(User user) {
        try {
            return new UserVoiceTextResult(new VoiceText(user), false);
        } catch (VoiceText.WrongException e) {
            new DefaultParamsManager(user).setDefaultVoiceText(null);
            return new UserVoiceTextResult(new VoiceText(), true);
        }
    }

    public static record UserVoiceTextResult(VoiceText vt, boolean isReset) {
        public VoiceText getVoiceText() {
            return vt;
        }
    }
}
