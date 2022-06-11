package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * When someone moves, if there are more non-Bot users in the destination than in the source, they will be moved.
 */
public class AutoMove extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        AudioChannel oldChannel = event.getOldValue();
        AudioChannel newChannel = event.getNewValue();
        long newUsers = newChannel.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        new LibFlow("AutoMove")
            .action("%s: %s -> %s (%s)".formatted(
                event.getMember().getUser().getAsTag(),
                oldChannel.getName(),
                newChannel.getName(),
                newUsers));

        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            return; // 自身がどのVCにも参加していない
        }
        AudioChannel connectedChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        long connectedUsers = newChannel.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        if (event.getMember().getUser().isBot()) {
            return;
        }

        if (event.getGuild().getSelfMember().getVoiceState().getChannel() != oldChannel) {
            return; // 移動元チャンネルに自身が入っていない
        }

        if (connectedUsers >= newUsers) {
            return; // 自身がいるチャンネルの人数より、移動先の人数の方が少ない、もしくは同じ場合終了
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getChannelJoined());

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        new LibFlow("AutoJoin").success("自動移動しました: %s -> %s", connectedChannel.getName(), newChannel.getName());
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoMoved")
            .setDescription("<#%s> から <#%s> に移動しました。".formatted(
                connectedChannel.getId(),
                newChannel.getId()
            ))
            .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
    }
}
