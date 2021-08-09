package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Cmd_Title implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":regional_indicator_t:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "参加中VCにタイトルを設定します")
                    .addOption(OptionType.STRING, "title", "設定するタイトル", true)
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        title(member, event);
    }

    void title(Member member, SlashCommandEvent event) {
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":no_entry_sign: VCに入ってから実行してください")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        String new_title = Main.getExistsOption(event, "title").getAsString();
        VoiceChannel targetVC = member.getVoiceState().getChannel();

        LibTitle libTitle = Main.getLibTitle();
        if (libTitle == null) {
            event.replyEmbeds(new EmbedBuilder()
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
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setDescription("何らかのエラーが発生したため、VC名の変更に失敗しました。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }


        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":magic_wand: タイトルを変更しました！")
            .setDescription(String.format("`%s` -> `%s`\n\n全員退出したらリセットされます。%s",
                old_title,
                new_title,
                isInitialized ? "\n初期設定がされていなかったため、元のチャンネル名をデフォルトとして登録しました。" : ""))
            .setColor(LibEmbedColor.success)
            .build()
        ).queue(
            msg -> msg.retrieveOriginal().queue(
                origin_msg -> new VoiceText()
                    .play(origin_msg, String.format("タイトルを%sに変更しました", new_title))
            )
        );
    }
}
