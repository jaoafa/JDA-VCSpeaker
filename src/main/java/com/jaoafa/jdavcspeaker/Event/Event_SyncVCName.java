package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Event_SyncVCName extends ListenerAdapter {
    @Override
    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {
        // 新しい値がcurrentかoriginalと同じだったらVCSpeakerの作業だからSkip
        VoiceChannel vc = event.getChannel();
        LibTitle libTitle = Main.getLibTitle();
        if (libTitle == null) {
            return;
        }

        if (!libTitle.existsTitle(vc)) {
            return;
        }

        if (event.getNewName().equals(libTitle.getCurrentTitle(vc))) {
            return;
        }

        if (event.getNewName().equals(libTitle.getOriginalTitle(vc))) {
            return;
        }

        //もし手動で変更されたらOriginalとして設定
        libTitle.saveAsOriginal(event.getChannel());
    }
}
