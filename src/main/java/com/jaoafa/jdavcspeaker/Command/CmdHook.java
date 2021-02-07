package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Util.ErrorReporter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class CmdHook {
    @SubscribeEvent
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        JDA jda = event.getJDA();
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        Message message = event.getMessage();
        String text = event.getMessage().getContentRaw();

        if (message.isWebhookMessage()) {
            return;
        }
        if (!text.startsWith("^")) {
            return;
        }

        if (text.equals("^")) {
            return;
        }

        String[] args;
        String cmdname;
        if (text.contains(" ")) {
            cmdname = text.split(" ")[0].substring(1).trim();
            args = Arrays.copyOfRange(text.split(" "), 1, text.split(" ").length);
            args = Arrays.stream(args)
                    .filter(s -> (s != null && s.length() > 0))
                    .toArray(String[]::new);
        } else {
            args = new String[] {};
            cmdname = text.substring(1).trim();
        }
        try {
            String className = cmdname.substring(0, 1).toUpperCase() + cmdname.substring(1).toLowerCase();
            Class.forName("com.jaoafa.jdavcspeaker.Command.Cmd_" + className);
            Constructor<?> construct = Class.forName("com.jaoafa.jdavcspeaker.Command.Cmd_" + className).getConstructor();
            CmdInterface cmd = null;
            try {
                cmd = (CmdInterface) construct.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            cmd.onCommand(jda, guild, channel, member, message, args);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            ErrorReporter.report("コマンドが見つかりません！",null,channel);
        }
    }
}
