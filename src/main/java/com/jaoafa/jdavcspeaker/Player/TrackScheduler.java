package com.jaoafa.jdavcspeaker.Player;

import com.jaoafa.jdavcspeaker.StaticData;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Map<String, Map.Entry<AudioPlayer, PlayerManager>> players = new HashMap<>();
    public final BlockingQueue<AudioTrack> queue;
    private final AudioPlayer player;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            String[] userdata = track.getUserData().toString().split("/");
            Message msg = StaticData.jda.getTextChannelById(userdata[0]).retrieveMessageById(userdata[1]).complete();
            msg.addReaction("✅").complete();
        }
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    private PlayerManager getTrackManager(Guild guild) {
        return players.get(guild.getId()).getValue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            String[] userdata = track.getUserData().toString().split("/");
            Message msg = StaticData.jda.getTextChannelById(userdata[0]).retrieveMessageById(userdata[1]).complete();
            msg.removeReaction("✅", StaticData.jda.getSelfUser()).complete();
            nextTrack();
        }
    }
}

