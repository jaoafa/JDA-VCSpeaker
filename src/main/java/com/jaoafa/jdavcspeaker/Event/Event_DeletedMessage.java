package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Player.GuildMusicManager;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Event_DeletedMessage extends ListenerAdapter {
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        final Guild guild = event.getGuild();
        final long messageId = event.getMessageIdLong();
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getGuildMusicManager(guild);

        new LibFlow("DeletedMessage").action("メッセージ(ID: " + messageId + ")が削除されました。");

        // キューにあるトラックが削除されたメッセージであるか
        musicManager.scheduler.queue.removeIf(track -> {
            if (!(track.getUserData() instanceof TrackInfo info)) {
                return false;
            }
            return info.getMessage().getIdLong() == messageId;
        });

        // 再生中のトラックが削除されたメッセージであるか
        AudioTrack playingTrack = musicManager.scheduler.player.getPlayingTrack();
        if (playingTrack != null &&
            playingTrack.getUserData() instanceof TrackInfo info
            && info.getMessage().getIdLong() == messageId) {
            musicManager.scheduler.nextTrack();
        }
    }
}
