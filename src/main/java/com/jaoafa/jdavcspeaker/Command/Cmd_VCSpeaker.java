package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import cloud.commandframework.jda.parsers.ChannelArgument;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_VCSpeaker implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .literal("server")
                        .literal("add")
                        .handler(context -> execute(context, this::addServer, false))
                        .build(),
                builder
                        .literal("server")
                        .literal("remove")
                        .handler(context -> execute(context, this::removeServer))
                        .build(),
                builder
                        .literal("server")
                        .literal("notifychannel")
                        .argument(ChannelArgument
                                .<JDACommandSender>newBuilder("channel")
                                .withParsers(new HashSet<>(Arrays.asList(ChannelArgument.ParserMode.values()))))
                        .handler(context -> execute(context, this::setNotifyChannel))
                        .build()
        );
    }

    void addServer(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply("あなたには管理者権限がないため、このサーバでVCSpeakerの設定をすることはできません。").queue();
            return;
        }
        if (MultipleServer.isTargetServer(guild)) {
            message.reply(MessageFormat.format("既にこのサーバは登録されており、<#{0}> がVCチャンネルとして登録されています。",
                    String.valueOf(MultipleServer.getVCChannelId(guild)))).queue();
            return;
        }
        boolean bool = MultipleServer.addServer(guild, channel);
        message.reply(MessageFormat.format("このサーバの登録に{0}しました。{1}",
                bool ? "成功" : "失敗",
                bool ? MessageFormat.format("<#{0}>がVCチャンネルとして登録されました。", String.valueOf(MultipleServer.getVCChannelId(guild))) : "")).queue();
    }

    void removeServer(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply("あなたには管理者権限がないため、このサーバでVCSpeakerの設定をすることはできません。").queue();
            return;
        }
        if (!MultipleServer.isTargetServer(guild)) {
            message.reply("このサーバは登録されていません。").queue();
            return;
        }
        boolean bool = MultipleServer.removeServer(guild);
        message.reply(MessageFormat.format("このサーバの登録解除に{0}しました。", bool ? "成功" : "失敗")).queue();
    }

    void setNotifyChannel(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        MessageChannel notifyChannel = context.getOrDefault("channel", null);
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply("あなたには管理者権限がないため、このサーバでVCSpeakerの設定をすることはできません。").queue();
            return;
        }
        if (MultipleServer.isNotifiable(guild)) {
            message.reply(MessageFormat.format("既にこのサーバの通知チャンネルは登録されており、<#{0}> が通知チャンネルとして登録されています。",
                    String.valueOf(MultipleServer.getNotifyChannelId(guild)))).queue();
            return;
        }
        if (notifyChannel == null) {
            boolean bool = MultipleServer.removeNotifyChannel(guild);
            message.reply(MessageFormat.format("このサーバの通知チャンネル解除に{0}しました。", bool ? "成功" : "失敗")).queue();
            return;
        }
        boolean bool = MultipleServer.setNotifyChannel(guild, notifyChannel);
        message.reply(MessageFormat.format("このサーバの通知チャンネル登録に{0}しました。{1}",
                bool ? "成功" : "失敗",
                bool ? MessageFormat.format("<#{0}>が通知チャンネルとして登録されました。", String.valueOf(MultipleServer.getNotifyChannelId(guild))) : "")).queue();
    }
}
