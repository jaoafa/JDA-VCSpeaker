package com.jaoafa.jdavcspeaker.Player;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

public record TrackInfo(SpeakFromType speakFromType, Message message) {
    public Message getMessage() {
        return message;
    }

    public User getUser() {
        return message.getAuthor();
    }

    public MessageChannelUnion getChannel() {
        return message.getChannel();
    }

    public SpeakFromType getSpeakFromType() {
        return speakFromType;
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
            "speakFromType=" + speakFromType +
            ", message=" + message +
            '}';
    }

    public enum SpeakFromType {
        /** 通常メッセージが送信された */
        RECEIVED_MESSAGE,
        /** ファイルが送信された */
        RECEIVED_FILE,
        /** 画像が送信された */
        RECEIVED_IMAGE,
        /** スタンプが送信された */
        RECEIVED_STICKER,
        /** VCにユーザーが参加した */
        JOINED_VC,
        /** VCでユーザーが移動した */
        MOVED_VC,
        /** VCからユーザーが退出した */
        QUITED_VC,
        /** GoLiveを開始した */
        STARTED_GOLIVE,
        /** GoLiveを終了した */
        ENDED_GOLIVE,
        /** VCタイトルを変えた */
        CHANGED_TITLE,
        /** スレッドを作成した */
        CREATED_THREAD
    }
}
