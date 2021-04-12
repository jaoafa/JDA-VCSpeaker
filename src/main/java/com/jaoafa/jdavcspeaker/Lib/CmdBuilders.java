package com.jaoafa.jdavcspeaker.Lib;

import cloud.commandframework.Command;
import cloud.commandframework.jda.JDACommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdBuilders {
    private final Command<JDACommandSender>[] commands;

    @SafeVarargs
    public CmdBuilders(Command<JDACommandSender>... commands) {
        this.commands = commands;
    }

    /**
     * Commandリストを返します
     *
     * @return Commandリスト
     */
    public List<Command<JDACommandSender>> getCommands() {
        return Arrays.asList(this.commands);
    }
}
