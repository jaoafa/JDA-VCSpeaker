package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.EmojiWrapper;
import com.jaoafa.jdavcspeaker.Lib.LibIgnore;
import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.htmlparser.jericho.Source;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 「ユーザーが送信したデフォルトメッセージ」のメッセージプロセッサ
 * <p>
 * ・メッセージ本文自体を原則そのまま読み上げる。（エイリアス等は適用する）
 * ・画像など添付ファイルは ProcessorType.ATTACHMENTS で処理する。
 */
public class DefaultMessageProcessor implements BaseProcessor {
    final Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    final Pattern messageUrlPattern = Pattern.compile("^https://.*?discord(?:app)?\\.com/channels/(\\d+)/(\\d+)/(\\d+)\\??(.*)$", Pattern.CASE_INSENSITIVE);
    final Pattern eventDirectLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?discord(?:app)?\\.com/events/(\\d+)/(\\d+)$", Pattern.CASE_INSENSITIVE);
    final Pattern eventInviteLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:discord(?:app)?\\.com/invite|discord\\.gg)/(\\w+)\\?event=(\\d+)$", Pattern.CASE_INSENSITIVE);
    final Pattern inviteLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:discord(?:app)?\\.com/invite|discord\\.gg)/(\\w+)$", Pattern.CASE_INSENSITIVE);
    final Pattern tweetUrlPattern = Pattern.compile("^https://twitter\\.com/(\\w){1,15}/status/(\\d+)\\??(.*)$", Pattern.CASE_INSENSITIVE);
    final Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    final Pattern spoilerPattern = Pattern.compile("\\|\\|.+\\|\\|");
    final Pattern channelReplyPattern = Pattern.compile("<#(\\d+)>");

    @Override
    public ProcessorType getType() {
        return ProcessorType.DEFAULT;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        speak(jda, guild, message, uvtr, message.getContentDisplay());
    }

    public void speak(JDA jda, Guild guild, Message message, UserVoiceTextResult uvtr, String speakContent) {
        if (LibIgnore.isIgnoreMessage(speakContent)) {
            return;
        }

        // Replace discord invite(include event) url
        speakContent = replacerDiscordInviteLink(jda, guild, speakContent);
        // Replace url
        speakContent = replacerLink(jda, speakContent);
        // Spoiler
        speakContent = replacerSpoiler(speakContent);
        // Thread reply
        speakContent = replacerChannelThreadLink(jda, speakContent);
        // Emphasize
        boolean isEmphasize = isEmphasizeMessage(speakContent);
        if (isEmphasize) {
            speakContent = replacerEmphasizeMessage(speakContent);
        }

        VoiceText vt = isEmphasize ? changeEmphasizeSpeed(uvtr.getVoiceText()) : uvtr.getVoiceText();
        vt.play(
            TrackInfo.SpeakFromType.RECEIVED_MESSAGE,
            message,
            speakContent
        );
    }

    /**
     * チャンネルIDをもとに、チャンネルまたはスレッドを取得します<br>
     * VCSpeakerが参加しているテキストチャンネルとスレッドに対応しますが、アーカイブされているスレッドには対応していません。
     *
     * @param jda       JDA
     * @param channelId チャンネルID (or スレッドID)
     *
     * @return チャンネル、見つからなければnull
     */
    MessageChannel getChannelOrThread(JDA jda, String channelId) {
        TextChannel textChannel = jda.getTextChannelById(channelId);
        if (textChannel != null) {
            return textChannel;
        }
        return jda.getThreadChannelById(channelId);
    }

    /**
     * チャンネルIDをもとに、テキスト/ボイスチャンネルまたはスレッドを取得します<br>
     * VCSpeakerが参加しているテキスト/ボイスチャンネルとスレッドに対応しますが、アーカイブされているスレッドには対応していません。
     *
     * @param jda       JDA
     * @param channelId チャンネルID (or スレッドID)
     *
     * @return チャンネル、見つからなければnull
     */
    Channel getTextVoiceChannelOrThread(JDA jda, String channelId) {
        TextChannel textChannel = jda.getTextChannelById(channelId);
        if (textChannel != null) {
            return textChannel;
        }
        VoiceChannel voiceChannel = jda.getVoiceChannelById(channelId);
        if (voiceChannel != null) {
            return voiceChannel;
        }
        return jda.getThreadChannelById(channelId);
    }

    String replacerLink(JDA jda, String content) {
        Matcher m = urlPattern.matcher(content);
        while (m.find()) {
            String url = m.group();

            // Discordメッセージリンク
            Matcher msgUrlMatcher = messageUrlPattern.matcher(url);
            if (msgUrlMatcher.find()) {
                String channelId = msgUrlMatcher.group(2);
                String messageId = msgUrlMatcher.group(3);

                MessageChannel channel = getChannelOrThread(jda, channelId);
                if (channel == null) continue;
                Message message = channel.retrieveMessageById(messageId).complete();
                if (message == null) continue;

                channel = message.getChannel();
                String channelName = "チャンネル「" + channel.getName() + "」";
                if (channel instanceof ThreadChannel) {
                    channelName = "チャンネル「%s」のスレッド「%s」".formatted(((ThreadChannel) channel).getParentChannel().getName(), channel.getName());
                }

                content = content.replace(url, "%sが%sで送信したメッセージのリンク".formatted(
                    message.getAuthor().getAsTag(),
                    channelName
                ));
                continue;
            }

            Matcher tweetUrlMatcher = tweetUrlPattern.matcher(url);
            if (tweetUrlMatcher.find()) {
                String screenName = tweetUrlMatcher.group(1);
                String tweetId = tweetUrlMatcher.group(2);

                Tweet tweet = getTweet(screenName, tweetId);
                if (tweet != null) {
                    System.out.println(tweet);
                    content = content.replace(url, "%sのツイート「%s」へのリンク".formatted(
                        EmojiWrapper.removeAllEmojis(tweet.authorName()),
                        tweet.plainText()
                    ));
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
                content = content.replace(url, "Webページ「%s」へのリンク".formatted(title));
            } else {
                content = content.replace(url, "Webページへのリンク");
            }
        }
        return content;
    }

    String replacerChannelThreadLink(JDA jda, String content) {
        return channelReplyPattern.matcher(content).replaceAll(result -> {
            String channelId = result.group(1);
            Channel channel = getTextVoiceChannelOrThread(jda, channelId);
            if (channel == null) return "どこかのチャンネルへのリンク";
            String channelName = "チャンネル「" + channel.getName() + "」へのリンク";
            if (channel instanceof ThreadChannel) {
                channelName = "チャンネル「%s」のスレッド「%s」へのリンク".formatted(((ThreadChannel) channel).getParentChannel().getName(), channel.getName());
            }
            return channelName;
        });
    }

    String replacerDiscordInviteLink(JDA jda, Guild sendFromGuild, String content) {
        content = eventDirectLinkUrlPattern.matcher(content).replaceAll(result -> {
            String guildId = result.group(1);
            String eventId = result.group(2);

            Guild guild = jda.getGuildById(guildId);
            if (guild == null) return "どこかのサーバのイベントへのリンク";

            String eventName = getScheduledEventName(guildId, eventId);
            if (eventName == null) return "サーバ「%s」のイベントへのリンク".formatted(guild.getName());

            if (guild.getIdLong() == sendFromGuild.getIdLong()) {
                return "イベント「%s」へのリンク".formatted(eventName);
            }
            return "サーバ「%s」のイベント「%s」へのリンク".formatted(guild.getName(), eventName);
        });

        content = eventInviteLinkUrlPattern.matcher(content).replaceAll(result -> {
            String inviteCode = result.group(1);
            String eventId = result.group(2);

            DiscordInvite invite = getInvite(inviteCode, eventId);
            if (invite == null) return "どこかのサーバのイベントへのリンク";
            if (invite.eventName() == null) return "サーバ「%s」のイベントへのリンク".formatted(invite.guildName());

            if (invite.guildId().equals(sendFromGuild.getId())) {
                return "イベント「%s」へのリンク".formatted(invite.eventName());
            }
            return "サーバ「%s」のイベント「%s」へのリンク".formatted(invite.guildName(), invite.eventName());
        });

        content = inviteLinkUrlPattern.matcher(content).replaceAll(result -> {
            String inviteCode = result.group(1);

            DiscordInvite invite = getInvite(inviteCode, null);
            if (invite == null) return "どこかのサーバへの招待リンク";
            if (invite.channelName() == null) return "サーバ「%s」への招待リンク".formatted(invite.guildName());

            if (invite.guildId().equals(sendFromGuild.getId())) {
                return "チャンネル「%s」への招待リンク".formatted(invite.channelName());
            }
            return "サーバ「%s」のチャンネル「%s」への招待リンク".formatted(invite.guildName(), invite.channelName());
        });

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
                String plainText = new Source(html.replaceAll("<a.*>(.*)</a>", ""))
                    .getFirstElement("p")
                    .getRenderer()
                    .setMaxLineLength(Integer.MAX_VALUE)
                    .setNewLine(null)
                    .toString();
                System.out.println(plainText);
                return new Tweet(authorName, html, plainText);
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    String getScheduledEventName(String guildId, String eventId) {
        String url = "https://discord.com/api/guilds/%s/scheduled-events/%s".formatted(guildId, eventId);
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bot " + Main.getDiscordToken())
                .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 200 && response.code() != 302) {
                    return null;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    return null;
                }
                JSONObject json = new JSONObject(body.string());
                return json.getString("name");
            }
        } catch (IOException e) {
            return null;
        }
    }

    record DiscordInvite(
        String code,
        String guildId,
        String guildName,
        String channelId,
        String channelName,
        @Nullable String inviterId,
        @Nullable String inviterName,
        @Nullable String inviterDiscriminator,
        @Nullable String eventName,
        @Nullable String eventId
    ) {
    }

    @Nullable
    DiscordInvite getInvite(String inviteCode, String eventId) {
        String url = "https://discord.com/api/invites/%s".formatted(inviteCode);
        if (eventId != null) {
            url += "?guild_scheduled_event_id=%s".formatted(eventId);
        }
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
            Request request = new Request.Builder()
                .url(url)
                .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 200 && response.code() != 302) {
                    return null;
                }
                ResponseBody body = response.body();
                if (body == null) {
                    return null;
                }
                JSONObject json = new JSONObject(body.string());
                return new DiscordInvite(
                    json.getString("code"),
                    json.getJSONObject("guild").getString("id"),
                    json.getJSONObject("guild").getString("name"),
                    json.getJSONObject("channel").getString("id"),
                    json.getJSONObject("channel").getString("name"),
                    json.has("inviter") ? json.getJSONObject("inviter").getString("id") : null,
                    json.has("inviter") ? json.getJSONObject("inviter").getString("username") : null,
                    json.has("inviter") ? json.getJSONObject("inviter").getString("discriminator") : null,
                    json.has("guild_scheduled_event") ? json.getJSONObject("guild_scheduled_event").getString("name") : null,
                    json.has("guild_scheduled_event") ? json.getJSONObject("guild_scheduled_event").getString("id") : null
                );
            }
        } catch (IOException e) {
            return null;
        }
    }

}
