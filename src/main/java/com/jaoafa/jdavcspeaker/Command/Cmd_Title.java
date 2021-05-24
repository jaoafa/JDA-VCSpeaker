package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Title implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
            builder
                .handler(context -> execute(context, this::title))
                .argument(StringArgument.greedy("title"))
                .build()
        );
    }

    void title(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            message.reply(new EmbedBuilder()
                .setTitle(":no_entry_sign: VCに入ってから実行してください")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        String new_title = context.get("title");
        VoiceChannel targetVC = member.getVoiceState().getChannel();

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

        boolean isInitialized = false;
        String old_title = targetVC.getName();
        if (!libTitle.existsTitle(targetVC)) {
            isInitialized = libTitle.saveAsOriginal(targetVC);
        }
        boolean bool = libTitle.setTitle(member.getVoiceState().getChannel(), new_title);
        if (!bool) {
            message.reply(new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setDescription("何らかのエラーが発生したため、VC名の変更に失敗しました。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        message.reply(new EmbedBuilder()
            .setTitle(":magic_wand: タイトルを変更しました！")
            .setDescription(String.format("`%s` -> `%s`\n\n全員退出したらリセットされます。%s",
                old_title,
                new_title,
                isInitialized ? "\n初期設定がされていなかったため、元のチャンネル名をデフォルトとして登録しました。" : ""))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
