package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

/**
 * In Guild's all VCs except for the AFK channel, if there is no user except Bot, and someone joins the VC (except for move)
 * <p>
 * If it has been less than one hour since the last notification, no notification will be given.
 */
public class Event_GeneralNotify extends ListenerAdapter {
    long lastNotificationTime = 0L;

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (StaticData.textChannel == null) {
            return;
        }
        if (event.getGuild().getIdLong() != StaticData.textChannel.getGuild().getIdLong()) {
            return; // テキストチャンネルとGuildが違ったらreturn
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

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":inbox_tray: 会話が始まりました！")
            .setDescription(MessageFormat.format("{0}が`{1}`に参加しました。", event.getMember().getAsMention(), event.getChannelJoined().getName()))
            .setColor(LibEmbedColor.normal);
        StaticData.textChannel.sendMessage(embed.build()).queue();
    }
}
