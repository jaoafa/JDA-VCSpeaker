package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

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
        message.reply(new EmbedBuilder()
                .setTitle(":wave: 再起動します。")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue(
                m -> {
                    guild.getAudioManager().closeAudioConnection();
                    System.exit(0);
                }
        );
    }
}
