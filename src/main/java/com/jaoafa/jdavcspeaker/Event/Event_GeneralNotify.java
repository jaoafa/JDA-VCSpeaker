package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class Event_GeneralNotify {
    @SubscribeEvent
    public void onMemberJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (!event.getGuild().getId().equals("597378876556967936")){
            return;
        }
        if (event.getChannelJoined().getMembers().size() >= 2&&event.getGuild().getSelfMember().getVoiceState().getChannel() != event.getChannelJoined()){
            return;
        }
        if (event.getChannelJoined().getMembers().size() != 1){
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":inbox_tray: 会話が始まりました！");
        eb.setDescription(event.getMember().getAsMention()+String.format("が`%s`に参加しました。",event.getChannelJoined().getName()));
        event.getJDA().getTextChannelById("597419057251090443").sendMessage(eb.build()).queue();
    }
}
