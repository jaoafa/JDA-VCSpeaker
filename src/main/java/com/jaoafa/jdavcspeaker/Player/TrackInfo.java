package com.jaoafa.jdavcspeaker.Player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;

public class TrackInfo {
    private final AudioTrack track;
    private final Member author;

    TrackInfo(AudioTrack track, Member author) {
        this.track = track;
        this.author = author;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Member getAuthor() {
        return author;
    }

}
