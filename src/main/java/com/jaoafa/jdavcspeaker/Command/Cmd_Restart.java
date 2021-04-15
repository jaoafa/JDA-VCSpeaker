package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.entities.*;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;


public class Cmd_Restart implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(context -> execute(context, this::restart))
                .build()
        );
    }

    void restart(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if(!channel.getId().equals(StaticData.vcTextChannel)) return;
        System.exit(0);
    }
}
