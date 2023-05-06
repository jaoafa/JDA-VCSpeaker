package com.jaoafa.jdavcspeaker.Player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static final AudioPlayerManager playerManager;
    private static final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    static {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildID = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildID);
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildID, musicManager);
        }
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    public static void destroyGuildMusicManager(Guild guild) {
        getGuildMusicManager(guild).player.destroy();
        musicManagers.remove(guild.getIdLong());
    }

    public static void loadAndPlay(TrackInfo info, String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(info.getMessage().getGuild());
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(info);
                play(musicManager, track);
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

    private static void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public static Map<Long, GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }
}
