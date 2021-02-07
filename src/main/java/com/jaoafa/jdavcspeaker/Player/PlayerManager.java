package com.jaoafa.jdavcspeaker.Player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;


    private PlayerManager(){
        this.musicManagers=new HashMap<>();
        this.playerManager=new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild){
        long guildID = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildID);
        if (musicManager==null){
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildID,musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        System.out.println("queue....");
        musicManager.scheduler.queue(track);
    }

    public static synchronized PlayerManager getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE=new PlayerManager();
        }
        return INSTANCE;
    }

}
