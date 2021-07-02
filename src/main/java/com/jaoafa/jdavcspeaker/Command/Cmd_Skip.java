package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Skip implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(context -> execute(context, this::skip))
                .build()
        );
    }

    void skip(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).scheduler.nextTrack();
        message.reply(new EmbedBuilder()
            .setTitle(":track_next: 読み上げをスキップします。")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
