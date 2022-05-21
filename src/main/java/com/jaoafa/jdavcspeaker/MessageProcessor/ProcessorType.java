package com.jaoafa.jdavcspeaker.MessageProcessor;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum ProcessorType {
    /** ユーザーが送信したデフォルトメッセージ */
    DEFAULT((message) -> message.getType() == MessageType.DEFAULT && !message.getAuthor().isBot() && !message.getType().isSystem()),
    /** Bot が送信したデフォルトメッセージ */
    BOT_DEFAULT((message) -> message.getType() == MessageType.DEFAULT && message.getAuthor().isBot() && !message.getType().isSystem()),
    /** ユーザーがメッセージに対して返信したメッセージ (Embed メッセージを含む) */
    REPLY((message) -> message.getType() == MessageType.INLINE_REPLY && !message.getAuthor().isBot() && !message.getType().isSystem()),
    /** Bot がメッセージに対して返信したメッセージ (Embed メッセージを含む) */
    BOT_REPLY((message) -> message.getType() == MessageType.INLINE_REPLY && message.getAuthor().isBot() && !message.getType().isSystem()),
    /** Bot に対してのコマンドと思われるメッセージ */
    BOT_COMMAND((message) -> message.getType() == MessageType.DEFAULT && getBotCommandPrefix(message.getContentDisplay()) && !message.getType().isSystem()),
    /** ユーザーが送信したEmbedメッセージ */
    EMBED((message) -> message.getType() == MessageType.DEFAULT && message.getEmbeds().size() > 0 && !message.getAuthor().isBot() && !message.getType().isSystem()),
    /** Bot が送信したEmbedメッセージ */
    BOT_EMBED((message) -> message.getType() == MessageType.DEFAULT && message.getEmbeds().size() > 0 && message.getAuthor().isBot() && !message.getType().isSystem()),
    /** スタンプ (Sticker) メッセージ */
    STICKERS((message) -> message.getType() == MessageType.DEFAULT && message.getStickers().size() > 0 && !message.getType().isSystem()),
    /** 添付ファイル (メッセージ) */
    ATTACHMENTS((message) -> message.getType() == MessageType.DEFAULT && message.getAttachments().size() > 0 && !message.getType().isSystem()),
    /** スレッド開始通知メッセージ (メッセージ派生かどうかを問わない) */
    CREATED_THREAD((message) -> message.getType() == MessageType.THREAD_CREATED),
    /** スラッシュコマンドによるメッセージ */
    WITH_APPLICATION_COMMAND((message) -> message.getType() == MessageType.SLASH_COMMAND);

    private final Predicate<Message> predicate;

    ProcessorType(Predicate<Message> predicate) {
        this.predicate = predicate;
    }

    public boolean check(Message message) {
        return predicate.test(message);
    }

    public static List<ProcessorType> getMatchProcessor(Message message) {
        return Stream.of(values()).filter(p -> p.check(message)).toList();
    }

    static boolean getBotCommandPrefix(String content) {
        return Stream.of("/", "!").anyMatch(content::startsWith);
    }
}
