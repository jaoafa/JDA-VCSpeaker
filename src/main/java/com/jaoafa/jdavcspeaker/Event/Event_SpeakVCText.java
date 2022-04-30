package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.htmlparser.jericho.Source;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Event_SpeakVCText extends ListenerAdapter {
    final Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    final Pattern messageUrlPattern = Pattern.compile("^https://.*?discord(?:app)?\\.com/channels/([0-9]+)/([0-9]+)/([0-9]+)\\??(.*)$", Pattern.CASE_INSENSITIVE);
    final Pattern eventDirectLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?discord(?:app)?\\.com/events/([0-9]+)/([0-9]+)$", Pattern.CASE_INSENSITIVE);
    final Pattern eventInviteLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:discord(?:app)?\\.com/invite|discord\\.gg)/(\\w+)\\?event=([0-9]+)$", Pattern.CASE_INSENSITIVE);
    final Pattern inviteLinkUrlPattern = Pattern.compile("^(?:https?://)?(?:www\\.)?(?:discord(?:app)?\\.com/invite|discord\\.gg)/(\\w+)$", Pattern.CASE_INSENSITIVE);
    final Pattern tweetUrlPattern = Pattern.compile("^https://twitter\\.com/(\\w){1,15}/status/([0-9]+)\\??(.*)$", Pattern.CASE_INSENSITIVE);
    final Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
    final Pattern spoilerPattern = Pattern.compile("\\|\\|.+\\|\\|");
    final Pattern channelReplyPattern = Pattern.compile("<#([0-9]+)>");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        Guild guild = event.getGuild();
        if (!MultipleServer.isTargetServer(guild)) {
            return;
        }
        final JDA jda = event.getJDA();
        final TextChannel channel = event.getTextChannel();
        final Message message = event.getMessage();
        if (channel.getIdLong() != MultipleServer.getVCChannelId(guild)) {
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

        if (guild.getSelfMember().getVoiceState() == null ||
            guild.getSelfMember().getVoiceState().getChannel() == null) {
            // 自身がどこにも入っていない場合

            if (member.getVoiceState() != null &&
                member.getVoiceState().getChannel() != null) {
                // メッセージ送信者がどこかのVCに入っている場合

                guild.getAudioManager().openAudioConnection(member.getVoiceState().getChannel()); // 参加
                if (MultipleServer.getVCChannel(guild) != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(":white_check_mark: AutoJoined")
                        .setDescription("`" + member.getVoiceState().getChannel().getName() + "`へ自動接続しました。")
                        .setColor(LibEmbedColor.success);
                    MultipleServer.getVCChannel(guild).sendMessageEmbeds(embed.build()).queue();
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

        UserVoiceTextResult uvtr = getUserVoiceText(user);
        if (uvtr.isReset()) {
            message.reply("デフォルトパラメーターが不正であるため、リセットしました。").queue();
        }
        VoiceText vt = isEmphasize ? changeEmphasizeSpeed(uvtr.getVoiceText()) : uvtr.getVoiceText();
        vt.play(TrackInfo.SpeakFromType.RECEIVED_MESSAGE, message, speakContent);

        for (MessageSticker sticker : message.getStickers()) {
            vt.play(TrackInfo.SpeakFromType.RECEIVED_MESSAGE, message, "スタンプ「" + sticker.getName() + "」が送信されました。");
        }

        // 画像等
        VisionAPI visionAPI = Main.getVisionAPI();
        if (visionAPI == null) {
            message.getAttachments()
                .forEach(attachment -> vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "ファイル「" + attachment.getFileName() + "」が送信されました。"));
            return;
        }
        LibFiles.VDirectory.VISION_API_TEMP.mkdirs();
        for (Message.Attachment attachment : message.getAttachments()) {
            if (attachment.isSpoiler()) {
                vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "スポイラーファイルが送信されました。");
                return;
            }
            attachment
                .downloadToFile(LibFiles.VDirectory.VISION_API_TEMP.getPath().resolve(attachment.getFileName()).toString())
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
                            vt.play(TrackInfo.SpeakFromType.RECEIVED_FILE, message, "ファイル「%s」が送信されました。".formatted(attachment.getFileName()));
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
                            vt.play(TrackInfo.SpeakFromType.RECEIVED_IMAGE, message, "画像ファイル「%sを含む画像」が送信されました。".formatted(text.length() > 30 ? text.substring(0, 30) : text));

                            message.reply("```\n" + text.replaceAll("\n", " ") + "\n```").queue();
                        } else {
                            vt.play(TrackInfo.SpeakFromType.RECEIVED_IMAGE, message, "画像ファイル「%s」が送信されました。".formatted(attachment.getFileName()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
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
                    channelName = "チャンネル「" + ((ThreadChannel) channel).getParentChannel().getName() + "」のスレッド「" + channel.getName() + "」";
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
                channelName = "チャンネル「" + ((ThreadChannel) channel).getParentChannel().getName() + "」のスレッド「" + channel.getName() + "」へのリンク";
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
            if (eventName == null) return "サーバ「" + guild.getName() + "」のイベントへのリンク";

            if (guild.getIdLong() == sendFromGuild.getIdLong()) {
                return "イベント「" + eventName + "」へのリンク";
            }
            return "サーバ「" + guild.getName() + "」のイベント「" + eventName + "」へのリンク";
        });

        content = eventInviteLinkUrlPattern.matcher(content).replaceAll(result -> {
            String inviteCode = result.group(1);
            String eventId = result.group(2);

            DiscordInvite invite = getInvite(inviteCode, eventId);
            if (invite == null) return "どこかのサーバのイベントへのリンク";
            if (invite.eventName() == null) return "サーバ「" + invite.guildName() + "」のイベントへのリンク";

            if (invite.guildId().equals(sendFromGuild.getId())) {
                return "イベント「" + invite.eventName() + "」へのリンク";
            }
            return "サーバ「" + invite.guildName() + "」のイベント「" + invite.eventName() + "」へのリンク";
        });

        content = inviteLinkUrlPattern.matcher(content).replaceAll(result -> {
            String inviteCode = result.group(1);

            DiscordInvite invite = getInvite(inviteCode, null);
            if (invite == null) return "どこかのサーバへの招待リンク";
            if (invite.channelName() == null) return "サーバ「" + invite.guildName() + "」への招待リンク";

            if (invite.guildId().equals(sendFromGuild.getId())) {
                return "チャンネル「" + invite.channelName() + "」への招待リンク";
            }
            return "サーバ「" + invite.guildName() + "」のチャンネル「" + invite.channelName() + "」への招待リンク";
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
