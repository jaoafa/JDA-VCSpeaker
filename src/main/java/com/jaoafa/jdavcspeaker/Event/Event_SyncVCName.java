package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibJson;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Event_SyncVCName extends ListenerAdapter {
    @Override
    public void onVoiceChannelUpdateName(VoiceChannelUpdateNameEvent event) {
        //新しい値がcurrentかoriginalと同じだったら
        //VCSpeakerの作業だからSkip
        if (event.getNewName().equals(LibJson.readObject("./title.json").getJSONObject(event.getChannel().getId()).getString("current"))||
                event.getNewName().equals(LibJson.readObject("./title.json").getJSONObject(event.getChannel().getId()).getString("original"))){
            return;
        }
        //もし手動で変更されたらOriginalとして設定
        LibTitle.saveAsOriginal(event.getChannel());
    }
}
