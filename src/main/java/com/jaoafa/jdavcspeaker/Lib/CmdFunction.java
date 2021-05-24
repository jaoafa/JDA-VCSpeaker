package com.jaoafa.jdavcspeaker.Lib;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@FunctionalInterface
public interface CmdFunction {
    void execute(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context);
}