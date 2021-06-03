package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Vcname implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .literal("save")
                        .handler(context -> execute(context, this::save))
                        .build(),
                builder
                        .literal("saveall")
                        .handler(context -> execute(context, this::saveall))
                        .build()
        );
    }

    void save(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!member.getId().equals("492088741167366144") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply(new EmbedBuilder()
                    .setTitle(":no_pedestrians: 実行する権限がありません！")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":no_entry_sign: VCに入ってから実行してください")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        LibTitle libTitle = Main.getLibTitle();
        if (libTitle == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: 初期化に失敗しています")
                    .setDescription("タイトル機能の初期化に失敗しているため、この機能は動作しません。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        boolean bool = libTitle.saveAsOriginal(member.getVoiceState().getChannel());
        if (!bool) {
            message.reply(new EmbedBuilder()
                    .setTitle(":x: 保存に失敗しました。")
                    .setDescription("何らかのエラーが発生したため、VC名の保存に失敗しました。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        message.reply(new EmbedBuilder()
                .setTitle(":white_check_mark: 保存に成功しました！")
                .setDescription("VC名を保存しました。\n現在のVC名をデフォルトとして使用します。")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }

    void saveall(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (!member.getId().equals("492088741167366144") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            message.reply(new EmbedBuilder()
                    .setTitle(":no_pedestrians: 実行する権限がありません！")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        LibTitle libTitle = Main.getLibTitle();
        if (libTitle == null) {
            message.reply(new EmbedBuilder()
                    .setTitle(":warning: 初期化に失敗しています")
                    .setDescription("タイトル機能の初期化に失敗しているため、この機能は動作しません。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        boolean bool = libTitle.saveAsOriginalAll(guild);
        if (!bool) {
            message.reply(new EmbedBuilder()
                    .setTitle(":x: 保存に失敗しました。")
                    .setDescription("何らかのエラーが発生したため、VC名の保存に失敗しました。")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        message.reply(new EmbedBuilder()
                .setTitle(":white_check_mark: 保存に成功しました！")
                .setDescription("タイトル設定中のVCを除き、全てのVC名を保存しました。\n現在のVC名をデフォルトとして使用します。")
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
