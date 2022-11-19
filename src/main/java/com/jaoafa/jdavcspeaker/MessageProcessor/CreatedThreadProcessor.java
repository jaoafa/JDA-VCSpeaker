package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * スレッド作成メッセージプロセッサ
 * <p>
 * ・スレッド作成時に自動投稿されるスレッド作成メッセージを処理する。
 */
public class CreatedThreadProcessor implements BaseProcessor {
    @Override
    public ProcessorType getType() {
        return ProcessorType.CREATED_THREAD;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        // 特定のメッセージからの派生としてスレッドを作成する場合は本プロセッサは動作しない可能性がある

        String threadName = message.getContentRaw(); // message.getStartedThread() が必ず null になるので、暫定的にこれで代用

        uvtr.vt().play(TrackInfo.SpeakFromType.CREATED_THREAD, message, "%s がスレッド「 %s 」を開始しました。".formatted(member.getUser().getName(), threadName));
    }
}
