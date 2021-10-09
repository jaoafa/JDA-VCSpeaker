package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class LibReporter {
    public LibReporter(MessageChannel channel, Exception e) {
        e.printStackTrace();

        if (channel != null) {
            StringBuilder stacktrace = new StringBuilder();

            for (StackTraceElement stackTraceElement : e.getStackTrace())
                stacktrace.append(stackTraceElement.getClassName()).append("\n");

            String stacktraceString = stacktrace.length() > 900 ? stacktrace.substring(0, 800) : stacktrace.toString();
            channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(":loudspeaker: 例外が発生しました！")
                .addField(":pencil: メッセージ", "```\n%s\n```".formatted(e.getMessage()), false)
                .addField(":boom: 原因", "```\n%s\n```".formatted(e.getCause()), false)
                .addField(":triangular_flag_on_post: スタックトレース", "```\n%s\n```".formatted(stacktraceString), false)
                .build()
            ).queue();
        }
        if (LibValue.rollbar != null) {
            LibValue.rollbar.error(e);
        }
    }
}
