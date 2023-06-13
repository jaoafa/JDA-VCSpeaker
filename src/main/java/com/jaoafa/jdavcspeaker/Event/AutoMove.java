package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

/**
 * When someone moves, if there are more non-Bot users in the destination than in the source, they will be moved.
 */
public class AutoMove extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (Main.getArgs().isDisableAutoMove) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        AudioChannel oldChannel = event.getOldValue();
        AudioChannel newChannel = event.getNewValue();
        if (oldChannel == null || newChannel == null) return; // 移動以外は除外
        long connectedUsers = getConnectedUsers(oldChannel);
        long newUsers = getConnectedUsers(newChannel);

        new LibFlow("AutoMove")
            .action("%s: %s (%d) -> %s (%s)".formatted(
                event.getMember().getUser().getAsTag(),
                oldChannel.getName(),
                connectedUsers,
                newChannel.getName(),
                newUsers));

        MoveInfo info = new MoveInfo(event, oldChannel, newChannel, connectedUsers, newUsers);
        runJoined(info);
        runNotJoined(info);
    }

    /**
     * Bot自身がVCに参加している場合に動作。
     * <p>
     * 移動先がAFKでなく、移動先にBot自身がおらず、移動先に参加しているユーザーが移動元より多い場合、移動する。
     *
     * @param info MoveInfo
     */
    void runJoined(MoveInfo info) {
        GuildVoiceUpdateEvent event = info.event();
        AudioChannel oldChannel = info.oldChannel();
        AudioChannel newChannel = info.newChannel();
        long connectedUsers = info.connectedUsers();
        long newUsers = info.newUsers();

        AudioChannel connectedChannel = getConnectedChannel(event);
        if (connectedChannel == null) {
            return; // VCに参加していない
        }

        if (event.getMember().getUser().isBot()) {
            return; // Botが移動した場合は終了
        }

        if (isAfkChannel(newChannel)) {
            return; // 移動先がAFKチャンネルの場合は終了
        }

        if (connectedChannel.getIdLong() != oldChannel.getIdLong()) {
            return; // 移動元チャンネルに自身が入っていない
        }

        if (connectedUsers >= newUsers) {
            return; // 自身がいるチャンネルの人数より、移動先の人数の方が少ない、もしくは同じ場合終了
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(event.getChannelJoined());

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        new LibFlow("AutoMove").success("自動移動しました: %s -> %s", connectedChannel.getName(), newChannel.getName());
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoMoved")
            .setDescription("<#%s> から <#%s> に移動しました。".formatted(
                connectedChannel.getId(),
                newChannel.getId()
            ))
            .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
    }

    /**
     * Bot自身がVCに参加していない場合に動作。
     * <p>
     * 移動元がAFKの場合、移動する。
     *
     * @param info MoveInfo
     */
    void runNotJoined(MoveInfo info) {
        GuildVoiceUpdateEvent event = info.event();
        AudioChannel oldChannel = info.oldChannel();
        AudioChannel newChannel = info.newChannel();

        AudioChannel connectedChannel = getConnectedChannel(event);
        if (connectedChannel != null) {
            return; // VCに参加している
        }

        if (!isAfkChannel(oldChannel)) {
            return; // 移動元がAFKチャンネルでない
        }

        if (isAfkChannel(newChannel)) {
            return; // 移動先がAFKチャンネルの場合は終了
        }

        if (event.getMember().getUser().isBot()) {
            return; // Botが移動した場合は終了
        }


        AudioManager audioManager = event.getGuild().getAudioManager();
        audioManager.openAudioConnection(newChannel);

        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        new LibFlow("AutoMove").success("自動接続しました: %s", newChannel.getName());
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":white_check_mark: AutoJoin (Triggered by move from AFK)")
            .setDescription("<#%s> に接続しました。".formatted(newChannel.getId()))
            .setColor(LibEmbedColor.success);
        MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
    }

    record MoveInfo(GuildVoiceUpdateEvent event, AudioChannel oldChannel, AudioChannel newChannel, long connectedUsers,
                    long newUsers) {
    }

    int getConnectedUsers(AudioChannel channel) {
        return (int) channel.getMembers().stream()
            .filter(member -> !member.getUser().isBot())
            .count();
    }

    AudioChannel getConnectedChannel(GuildVoiceUpdateEvent event) {
        return event.getGuild().getAudioManager().getConnectedChannel();
    }

    boolean isAfkChannel(AudioChannel channel) {
        if (channel.getGuild().getAfkChannel() == null) return false;
        return channel.getIdLong() == channel.getGuild().getAfkChannel().getIdLong();
    }
}
