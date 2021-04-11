package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_GeneralNotify extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (!event.getGuild().getId().equals("597378876556967936")) {
            return;
        }
        if (event.getChannelJoined().getMembers().size() >= 2 && event.getGuild().getSelfMember().getVoiceState().getChannel() != event.getChannelJoined()) {
            return;
        }
        if (event.getChannelJoined().getMembers().size() != 1) {
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":inbox_tray: 会話が始まりました！");
        eb.setDescription(event.getMember().getAsMention() + String.format("が`%s`に参加しました。", event.getChannelJoined().getName()));
        eb.setColor(LibEmbedColor.normal);
        event.getJDA().getTextChannelById("597419057251090443").sendMessage(eb.build()).queue();
    }
}
