package com.jaoafa.jdavcspeaker.Player;

import com.jaoafa.jdavcspeaker.StaticData;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
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
        if (player.startTrack(track, true)) {
            // トラックが開始された場合
            reactionSpeaking(track);
        }else{
            // トラックが開始されず、キューに挿入するべき場合
            queue.offer(track);
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        if(track == null){
            return;
        }
        player.startTrack(track, false);
        reactionSpeaking(track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (!(track.getUserData() instanceof TrackInfo)) {
                return;
            }
            TrackInfo info = (TrackInfo) track.getUserData();
            if (info == null) {
                return;
            }

            TextChannel channel = StaticData.jda.getTextChannelById(info.getChannel().getIdLong());
            if (channel == null) {
                return; // channelはnullである可能性がある
            }
            channel.retrieveMessageById(info.getMessage().getIdLong())
                .queue(msg -> msg.removeReaction("\uD83D\uDDE3", StaticData.jda.getSelfUser()) // :speaking_head:
                        .queue(null, Throwable::printStackTrace),
                    Throwable::printStackTrace);
            nextTrack();
        }
    }

    void reactionSpeaking(AudioTrack track){
        if (!(track.getUserData() instanceof TrackInfo)) {
            return;
        }
        TrackInfo info = (TrackInfo) track.getUserData();
        if (info == null) {
            return;
        }
        TextChannel channel = StaticData.jda.getTextChannelById(info.getChannel().getIdLong());
        if (channel == null) {
            return; // channelはnullである可能性がある
        }
        channel.retrieveMessageById(info.getMessage().getIdLong())
            .queue(msg -> msg.addReaction("\uD83D\uDDE3") // :speaking_head:
                    .queue(null, Throwable::printStackTrace),
                Throwable::printStackTrace);
    }
}

