package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

@FunctionalInterface
public interface CmdFunction {
    void execute(Guild guild, MessageChannel channel, Member member, Message message);

    class NullCommandExecutionHandler implements CmdFunction {
        @Override
        public void execute(Guild guild, MessageChannel channel, Member member, Message message) {
        }
    }
}