package com.jaoafa.jdavcspeaker.Player;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class TrackInfo {
    private final Message message;

    public TrackInfo(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public TextChannel getChannel() {
        return message.getTextChannel();
    }

}
