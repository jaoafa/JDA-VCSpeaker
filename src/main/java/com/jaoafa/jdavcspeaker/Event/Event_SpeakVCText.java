package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Event_SpeakVCText extends ListenerAdapter {
    Pattern urlPattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
    Pattern messageUrlPattern = Pattern.compile("^https://.*?discord\\.com/channels/([0-9]+)/([0-9]+)/([0-9]+)$", Pattern.CASE_INSENSITIVE);
    Pattern titlePattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        final JDA jda = event.getJDA();
        final TextChannel channel = event.getTextChannel();
        final Message message = event.getMessage();
        if (channel.getIdLong() != StaticData.textChannelId) {
            return; // VCテキストチャンネル以外からのメッセージ
        }
        final Member member = event.getMember();
        if (member == null) {
            return;
        }

        if (member.getUser().isBot()) {
            return;
        }

        final String content = message.getContentDisplay(); // チャンネル名・リプライとかが表示通りになっている文字列が返る
        if (content.equals(".")) {
            return; // .のみは除外
        }
        if (content.startsWith(";")) {
            return; // ;から始まるコマンドと思われる文字列を除外
        }

        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            // 自身がどこにも入っていない場合

            if (member.getVoiceState() != null &&
                member.getVoiceState().getChannel() != null) {
                // メッセージ送信者がどこかのVCに入っている場合

                event.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel()); // 参加
                if (StaticData.textChannel != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(":white_check_mark: AutoJoined")
                        .setDescription("`" + member.getVoiceState().getChannel().getName() + "`へ自動接続しました。")
                        .setColor(LibEmbedColor.success);
                    StaticData.textChannel.sendMessage(embed.build()).queue();
                }
            } else {
                return; // 自身がどこにも入っておらず、送信者もどこにも入っていない場合
            }
        }

        // ignore
        boolean ignoreEquals = StaticData.ignoreMap.entrySet().stream()
            .anyMatch(entry -> entry.getKey().equals("equal") &&
                content.equals(entry.getValue()));
        boolean ignoreContain = StaticData.ignoreMap.entrySet().stream()
            .anyMatch(entry -> entry.getKey().equals("contain") &&
                content.contains(entry.getValue()));

        if (ignoreEquals || ignoreContain) return;

        // 読み上げるメッセージの構築
        String speakContent = content;

        // Replace url
        speakContent = replacerLink(jda, speakContent);

        VoiceText.speak(message, speakContent);
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

            String title = getTitle(url);
            System.out.println("title: " + title);
            if (title.length() >= 30) {
                title = title.substring(0, 30) + "以下略";
            }
            System.out.println("title 2: " + title);
            content = content.replace(url, MessageFormat.format("Webページ「{0}」へのリンク", title));
        }
        return content;
    }

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
}
