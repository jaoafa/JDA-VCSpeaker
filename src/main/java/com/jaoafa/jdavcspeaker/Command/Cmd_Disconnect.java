package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cmd_Disconnect implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":octagonal_sign:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerを切断します")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        disconnect(guild, event);
    }


    void disconnect(Guild guild, SlashCommandInteractionEvent event) {
        if (guild.getSelfMember().getVoiceState() == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription("VoiceStateを取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        AudioChannel connectedChannel = guild.getSelfMember().getVoiceState().getChannel();
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
        cmdFlow.success("%s が %s から切断するようリクエストしました。", event.getUser().getAsTag(), connectedChannel.getName());

        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wave: 切断しました！")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
