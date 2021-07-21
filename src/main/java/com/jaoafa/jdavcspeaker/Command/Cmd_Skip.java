package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Cmd_Skip implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":broom:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "現在の読み上げをキャンセルします")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        skip(guild, event);
    }

    void skip(Guild guild, SlashCommandEvent event) {
        PlayerManager.getINSTANCE().getGuildMusicManager(guild).scheduler.nextTrack();
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":track_next: 読み上げをスキップします")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
