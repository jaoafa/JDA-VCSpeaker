package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface BaseProcessor {
    /** プロセッサの種別 */
    ProcessorType getType();

    /** 処理関数 */
    void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr);
}
