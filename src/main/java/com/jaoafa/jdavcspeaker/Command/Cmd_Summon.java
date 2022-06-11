package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cmd_Summon implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":star2:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerを召喚します")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        summon(guild, member, event);
    }

    void summon(Guild guild, Member member, SlashCommandInteractionEvent event) {
        if (member == null || member.getVoiceState() == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":warning: 何かがうまくいきませんでした…")
                .setDescription((member == null ? "Member" : member.getVoiceState() == null ? "VoiceState" : "") + "を取得できませんでした。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        AudioChannel connectedChannel = member.getVoiceState().getChannel();
        if (connectedChannel == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":warning: 接続に失敗しました")
                .setDescription("あなたはVCに参加していません。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        guild.getAudioManager().openAudioConnection(connectedChannel);
        cmdFlow.success("%s が %s に接続するようリクエストしました。", event.getUser().getAsTag(), connectedChannel.getName());

        event.replyEmbeds(new EmbedBuilder()
            .setDescription(":satellite: <#%s> に接続しました。".formatted(connectedChannel.getId()))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
