package com.jaoafa.jdavcspeaker.Util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;

public class ErrorReporter {
    public static void report(String title, String message, MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":woman_bowing: " + title);
        if (message != null) {
            eb.setDescription(message);
        }
        eb.setColor(new Color(255, 139, 139));
        channel.sendMessage(eb.build()).queue();
    }
}
