package com.jaoafa.jdavcspeaker;

import cloud.commandframework.Command;
import cloud.commandframework.jda.JDA4CommandManager;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public interface CmdInterface {
    CmdBuilders register(Command.Builder<JDACommandSender> builder);
}
