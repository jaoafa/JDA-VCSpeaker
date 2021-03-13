package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

public class Cmd_Summon implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        VoiceChannel connectedChannel = member.getVoiceState().getChannel();
        if(connectedChannel == null) {
            System.out.println("connectedChannel == null");
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(connectedChannel);
        System.out.println("joined!");
    }
}
