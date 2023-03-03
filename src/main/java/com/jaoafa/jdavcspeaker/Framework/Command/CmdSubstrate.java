package com.jaoafa.jdavcspeaker.Framework.Command;

import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CmdSubstrate {
    LibFlow cmdFlow = new LibFlow("Command");

    CmdDetail detail();

    void hooker(JDA jda, Guild guild, GuildMessageChannel channel, ChannelType type, Member member, User user, SlashCommandInteractionEvent event, String subCmd);
}
