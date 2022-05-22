package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * ユーザーのリプライメッセージプロセッサ
 * <p>
 * ・全部 DefaltProcessor で処理する
 * ・今後、必要に応じてリプライであることを明示的に発言するようにするかも…しれない
 */
public class ReplyProcessor implements BaseProcessor {
    @Override
    public ProcessorType getType() {
        return ProcessorType.REPLY;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        new DefaultMessageProcessor()
            .execute(jda, guild, channel, member, message, uvtr);
    }
}
