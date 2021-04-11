package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.Arrays;

public class Event_SpeakVCText {
    @SubscribeEvent
    public void onMsg(MessageReceivedEvent event) {
        StaticData.jda = event.getJDA();
        String msg = event.getMessage().getContentRaw();
        final boolean[] isIgnore = {false};

        if (!event.getChannel().getId().equals(StaticData.vcTextChannel)) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (event.getMessage().getContentRaw().equals(".")) {
            return;
        }

        VoiceChannel connectedChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (connectedChannel == null) {
            return;
        }

        StaticData.ignoreMap.forEach((k, v) -> {
            if (k.equals("equal")&&msg.equals(v)){
                isIgnore[0] = true;
            }
            if (k.equals("contain")&&msg.contains(v)){
                isIgnore[0] = true;
            }
        });
        if (isIgnore[0]) return;

        final String[] speaktext = {event.getMessage().getContentRaw()};

        Arrays.stream(speaktext[0].split(" ")).forEach(s -> {
            if (s.startsWith("http://") || s.startsWith("https://")) {
                int last = s.split("/").length;
                speaktext[0] = speaktext[0].replace(s, s.split("/")[last - 1]);
            }
        });

        VoiceText.speak(event.getTextChannel(), speaktext[0], event.getChannel().getId() + "/" + event.getMessage().getId());
    }
}
