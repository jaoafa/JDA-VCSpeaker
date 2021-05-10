package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

/**
 * If someone disconnects, it will also exit itself if there are no users other than the bot.
 */
public class AutoDisconnect extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            return; // 自身がどのVCにも参加していない
        }
        if (event.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong() != event.getChannelLeft().getIdLong()) {
            return; // 退出されたチャンネルが自身のいるVCと異なる
        }

        // VCに残ったユーザーが全員Bot、または誰もいなくなった
        boolean existsUser = event
                .getChannelLeft()
                .getMembers()
                .stream()
                .anyMatch(member -> !member.getUser().isBot()); // Bot以外がいるかどうか
        System.out.println(MessageFormat.format("[AutoDisconnect] {0}: {1} -> {2}",
                event.getMember().getUser().getAsTag(),
                event.getChannelLeft().getName(),
                !existsUser));

        if (existsUser) {
            return;
        }
        event.getGuild().getAudioManager().closeAudioConnection();

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":white_check_mark: AutoDisconnected")
                .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessage(embed.build()).queue();
    }
}
