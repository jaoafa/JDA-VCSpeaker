package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.CmdFunction;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Cmd_Clear implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .handler(context -> Main.cmdFunctionExecutor(context, this::clear))
                        .build()
        );
    }

    void clear(Guild guild, MessageChannel channel, Member member, Message message){
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).scheduler.queue.clear();
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).player.destroy();
        message.reply(new EmbedBuilder()
                .setTitle(":white_check_mark: 読み上げをクリアしました。")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
