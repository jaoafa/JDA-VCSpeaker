package com.jaoafa.jdavcspeaker.Framework.Command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public interface CmdSubstrate {
    CmdDetail detail();

    void hooker(JDA jda, Guild guild, MessageChannel channel, ChannelType type, Member member, User user, SlashCommandEvent event, String subCmd);
}
