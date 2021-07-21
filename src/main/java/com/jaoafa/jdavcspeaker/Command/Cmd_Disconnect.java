package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Disconnect implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":octagonal_sign:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerを切断します")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        disconnect(guild, event);
    }


    void disconnect(Guild guild, SlashCommandEvent event) {
        if (guild.getSelfMember().getVoiceState() == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":warning: 何かがうまくいきませんでした…")
                    .setDescription("VoiceStateを取得できませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        VoiceChannel connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
        if (connectedChannel == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: なにかがおかしいかも？")
                    .setDescription("VCSpeakerはVCに参加していません...")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }

        guild.getAudioManager().closeAudioConnection();

        event.replyEmbeds(new EmbedBuilder()
                .setTitle(":wave: 切断しました！")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
