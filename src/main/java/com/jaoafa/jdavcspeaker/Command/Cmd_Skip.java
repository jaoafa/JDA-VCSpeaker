package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cmd_Skip implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":broom:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "現在の読み上げをキャンセル（スキップ）します")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        skip(guild, event);
    }

    void skip(Guild guild, SlashCommandInteractionEvent event) {
        TrackScheduler scheduler = PlayerManager.getINSTANCE().getGuildMusicManager(guild).scheduler;
        if (scheduler.queue.isEmpty()) {
            scheduler.player.destroy();
        } else {
            scheduler.nextTrack();
        }
        cmdFlow.success("%s がスキップしました。", event.getUser().getAsTag());
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":track_next: 読み上げをスキップします")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
