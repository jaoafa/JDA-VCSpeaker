package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.StaticData;
import com.jaoafa.jdavcspeaker.Util.VoiceText;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Arrays;

public class Event_SpeakVCText {
    @SubscribeEvent
    public void onMsg(MessageReceivedEvent event) {
        StaticData.jda = event.getJDA();
        if (!event.getChannel().getId().equals(StaticData.vcTextChannel)){
            return;
        }
        if (event.getMember().getUser().isBot()){
            return;
        }
        if (event.getMessage().getContentRaw().equals(".")) {
            return;
        }
        VoiceChannel connectedChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if(connectedChannel == null) {
            return;
        }
        final String[] speaktext = {event.getMessage().getContentRaw()};

        Arrays.stream(speaktext[0].split(" ")).forEach(s ->{
            if (s.startsWith("http://")||s.startsWith("https://")){
                int last = s.split("/").length;
                speaktext[0] = speaktext[0].replace(s,s.split("/")[last-1]);
            }
        });

        VoiceText.speak(event.getTextChannel(), speaktext[0],event.getChannel().getId()+"/"+event.getMessage().getId());
    }
}
