package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Summon implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .handler(context -> execute(context, this::summon))
                        .build()
        );
    }

    void summon(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (member == null || member.getVoiceState() == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: 何かがうまくいきませんでした…")
                    .setDescription((member == null ? "Member" : member.getVoiceState() == null ? "VoiceState" : "") + "を取得できませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        VoiceChannel connectedChannel = member.getVoiceState().getChannel();
        if (connectedChannel == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: なにかがおかしいかも？")
                    .setDescription("VCに参加してからVCSpeakerを呼び出してください。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(connectedChannel);

        message.reply(new EmbedBuilder()
                .setTitle(":satellite: 接続しました！")
                .setDescription("`" + connectedChannel.getName() + "`に接続しました。")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
