package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class AutoJoin extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() == null || event.getChannelLeft() != null) return; // 参加以外は除外
        if (Main.getArgs().isDisableAutoJoin) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (event.getGuild().getSelfMember().getVoiceState() != null &&
            event.getGuild().getSelfMember().getVoiceState().getChannel() != null) {
            return; // 自身がいずれかのVCに参加している
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getChannelJoined());

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        new LibFlow("AutoJoin").success("自動接続しました: %s", event.getChannelJoined().getName());
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoJoin")
            .setDescription("<#%s> に接続しました。".formatted(event.getChannelJoined().getId()))
            .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
    }
}
