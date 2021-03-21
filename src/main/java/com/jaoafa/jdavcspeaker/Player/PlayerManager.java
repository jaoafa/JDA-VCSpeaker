package com.jaoafa.jdavcspeaker.Player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
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
    public void loadAndPlay(TextChannel channel, String trackUrl){
        GuildMusicManager musicManager=getGuildMusicManager(channel.getGuild());
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("Track Loaded on PlayerManager");
                play(musicManager,track);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {
                e.printStackTrace();
                System.out.println("loadFailed on PlayerManager");
            }
        });
    }
    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public static synchronized PlayerManager getINSTANCE(){
        if (INSTANCE == null){
            INSTANCE=new PlayerManager();
        }
        return INSTANCE;
    }

}
