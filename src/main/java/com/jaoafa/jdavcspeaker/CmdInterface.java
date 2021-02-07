package com.jaoafa.jdavcspeaker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public interface CmdInterface {
    public void onCommand(final JDA jda, final Guild guild, final MessageChannel channel, final Member member, final Message message, final String[] args);
}
