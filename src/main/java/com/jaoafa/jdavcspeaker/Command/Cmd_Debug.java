package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.File;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Debug implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(context -> execute(context, this::debug))
                .build()
        );
    }

    void debug(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!member.getId().equals("492088741167366144") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply(new EmbedBuilder()
                .setTitle(":no_pedestrians: あなたはデバッグを実行する権限がありません！")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        message.reply(new File("./title.json")).queue();
    }
}
