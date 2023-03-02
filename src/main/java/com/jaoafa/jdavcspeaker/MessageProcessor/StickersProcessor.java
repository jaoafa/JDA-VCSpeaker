package com.jaoafa.jdavcspeaker.MessageProcessor;

import com.jaoafa.jdavcspeaker.Lib.UserVoiceTextResult;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.sticker.StickerItem;

/**
 * スタンプ (Sticker) プロセッサ
 * <p>
 * ・「スタンプ XXX が送信されました。」と読み上げる
 */
public class StickersProcessor implements BaseProcessor {
    @Override
    public ProcessorType getType() {
        return ProcessorType.STICKERS;
    }

    @Override
    public void execute(JDA jda, Guild guild, TextChannel channel, Member member, Message message, UserVoiceTextResult uvtr) {
        for (StickerItem sticker : message.getStickers()) {
            uvtr.vt().play(TrackInfo.SpeakFromType.RECEIVED_STICKER, message, "スタンプ「 %s 」が送信されました。".formatted(sticker.getName()));
        }
    }
}
