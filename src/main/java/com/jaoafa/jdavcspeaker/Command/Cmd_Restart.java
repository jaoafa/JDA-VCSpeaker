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
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;


public class Cmd_Restart implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":expressionless:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerを再起動します")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        restart(guild, event);
    }


    void restart(Guild guild, SlashCommandInteractionEvent event) {
        cmdFlow.action("%s のリクエストにより、VCSpeakerを再起動します。", event.getUser().getAsTag());
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":wave: 再起動します")
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
