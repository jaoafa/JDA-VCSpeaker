package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

/**
 * ユーザーのリプライメッセージプロセッサ
 * <p>
 * ・全部 DefaultProcessor で処理する
 * ・「DiscordName へのリプライ、」を最初に入れる
 */
public class ReplyProcessor implements BaseProcessor {
    @Override
    public ProcessorType getType() {
        return ProcessorType.REPLY;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        MessageReference reference = message.getMessageReference();
        String content = message.getContentDisplay();
        if (reference != null && reference.getMessage() != null) {
            content = reference.getMessage().getAuthor().getName() + " へのリプライ、" + content;
        }
        new DefaultMessageProcessor()
            .speak(jda, guild, message, uvtr, content);
    }
}
