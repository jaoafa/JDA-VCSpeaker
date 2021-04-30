package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.text.MessageFormat;

/**
 * When someone moves, if there are more non-Bot users in the destination than in the source, they will be moved.
 */
public class AutoMove extends ListenerAdapter {
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        VoiceChannel oldChannel = event.getOldValue();
        VoiceChannel newChannel = event.getNewValue();
        long newUsers = newChannel.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        System.out.println(MessageFormat.format("[AutoMove] {0}: {1} -> {2} ({3})",
            event.getMember().getUser().getAsTag(),
            oldChannel.getName(),
            newChannel.getName(),
            newUsers));

        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            return; // 自身がどのVCにも参加していない
        }
        VoiceChannel connectedChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
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

        if (StaticData.textChannel == null) return;
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoMoved")
            .setDescription(MessageFormat.format("<#{0}> から <#{1}> に移動しました。", connectedChannel.getId(), newChannel.getId()))
            .setColor(LibEmbedColor.success);
        StaticData.textChannel.sendMessage(embed.build()).queue();
    }
}
