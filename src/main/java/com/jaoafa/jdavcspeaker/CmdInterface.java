package com.jaoafa.jdavcspeaker;

import cloud.commandframework.Command;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;

public interface CmdInterface {
    CmdBuilders register(Command.Builder<JDACommandSender> builder);
}
