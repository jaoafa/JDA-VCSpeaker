package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibAutoControl;
import com.jaoafa.jdavcspeaker.Lib.LibValue;
import com.jaoafa.jdavcspeaker.Lib.MsgFormatter.LibVisionAPI;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static com.jaoafa.jdavcspeaker.Lib.MsgFormatter.MsgFormatterUtils.*;

public class Event_SpeakVCText extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!MultipleServer.isTargetServer(event.getGuild())) return;

        final JDA jda = event.getJDA();
        final TextChannel channel = event.getTextChannel();
        final Message message = event.getMessage();

        // VCテキストチャンネル以外からのメッセージ
        if (channel.getIdLong() != MultipleServer.getVCChannelId(event.getGuild())) return;

        final Member member = event.getMember();
        if (member == null) return;

        User user = member.getUser();
        if (user.isBot()) return;

        // チャンネル名・リプライとかが表示通りになっている文字列が返る
        final String content = message.getContentDisplay();
        // .のみは除外
        if (content.equals(".")) return;
        // !から始まるコマンドと思われる文字列を除外
        if (content.startsWith("!")) return;

        //自動参加
        boolean isContinuable = LibAutoControl.join(event, member);
        if (!isContinuable) return;

        // ignore
        boolean ignoreEquals = LibValue.ignoreEquals.contains(content);
        boolean ignoreContain = LibValue.ignoreContains.stream().anyMatch(content::contains);

        if (ignoreEquals || ignoreContain) return;

        // 読み上げるメッセージの構築
        String speakContent = content;

        // Replace url
        speakContent = replacerLink(jda, speakContent);
        // Spoiler
        speakContent = replacerSpoiler(speakContent);
        // Emphasize
        boolean isEmphasize = isEmphasizeMessage(speakContent);
        if (isEmphasize) speakContent = replacerEmphasizeMessage(speakContent);


        UserVoiceTextResult uvtr = getUserVoiceText(user);
        if (uvtr.isReset()) message.reply("デフォルトパラメーターが不正であるため、リセットしました。").queue();

        VoiceText vt = isEmphasize ? changeEmphasizeSpeed(uvtr.getVoiceText()) : uvtr.getVoiceText();
        vt.play(TrackInfo.SpeakFromType.RECEIVED_MESSAGE, message, speakContent);

        // 画像等
        LibVisionAPI.check(message, vt);
    }
}
