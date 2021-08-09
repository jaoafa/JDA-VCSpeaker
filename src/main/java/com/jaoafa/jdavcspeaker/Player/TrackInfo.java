package com.jaoafa.jdavcspeaker.Player;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public record TrackInfo(Message message) {
    public Message getMessage() {
        return message;
    }

    public TextChannel getChannel() {
        return message.getTextChannel();
    }
}
