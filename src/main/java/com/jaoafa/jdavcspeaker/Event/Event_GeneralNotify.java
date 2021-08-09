package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;

/**
 * In Guild's all VCs except for the AFK channel, if there is no user except Bot, and someone joins the VC (except for move)
 * <p>
 * If it has been less than one hour since the last notification, no notification will be given.
 */
public class Event_GeneralNotify extends ListenerAdapter {
    long lastNotificationTime = 0L;

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (MultipleServer.getVCChannel(event.getGuild()) == null) {
            return;
        }
        if (event.getGuild().getIdLong() != MultipleServer.getVCChannel(event.getGuild()).getGuild().getIdLong()) {
            return; // テキストチャンネルとGuildが違ったらreturn
        }

        boolean isExistsJoinedChannel = event.getGuild().getVoiceChannels().stream()
            .filter(vc -> vc.getGuild().getAfkChannel() != null && // AFKチャンネルが定義されているうえで
                vc.getIdLong() != vc.getGuild().getAfkChannel().getIdLong()) // AFKチャンネル以外であり
            .filter(vc -> vc.getIdLong() != event.getChannelJoined().getIdLong()) // 今回参加されたチャンネル以外であり
            .anyMatch(vc -> vc.getMembers().stream().anyMatch(member -> !member.getUser().isBot())); // Bot以外のユーザーがいるチャンネルがあるか？
        if (isExistsJoinedChannel) {
            return; // 他のVCに誰か人がいる
        }

        long nonBotUsers = event
            .getChannelJoined()
            .getMembers()
            .stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        if (nonBotUsers != 1) {
            return;
        }

        if (lastNotificationTime >= System.currentTimeMillis() - 3600 * 1000) {
            // 最後の通知が1時間以内
            return;
        }
        lastNotificationTime = System.currentTimeMillis();

        Path notify_id_path = Paths.get("last-notify-id");

        if (Files.exists(notify_id_path)) {
            try {
                String last_notify_id = String.join("\n", Files.readAllLines(notify_id_path));
                //noinspection ResultOfMethodCallIgnored
                MultipleServer.getNotifyChannel(event.getGuild()).retrieveMessageById(last_notify_id).queue(Message::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":inbox_tray: 会話が始まりました！")
            .setDescription(MessageFormat.format("{0} が <#{1}> に参加しました。", event.getMember().getAsMention(), event.getChannelJoined().getId()))
            .setColor(LibEmbedColor.normal);
        MultipleServer.getNotifyChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue(
            message -> {
                try {
                    Files.write(notify_id_path, Collections.singleton(message.getId()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );
    }
}
