package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Cmd_Vcname implements CmdSubstrate {

    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":satellite_orbital:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VC名を保存します")
                    .addSubcommands(
                        new SubcommandData("save", "現在のVC名を保存します"),
                        new SubcommandData("saveall", "全てのVC名を保存します")
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        switch (subCmd) {
            case "save" -> save(member, event);
            case "saveall" -> saveall(guild, member, event);
        }
    }


    void save(Member member, SlashCommandInteractionEvent event) {
        if (!member.getId().equals("492088741167366144") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":no_pedestrians: 実行する権限がありません！")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":no_entry_sign: VCに入ってから実行してください")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
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
        boolean bool = libTitle.saveAsOriginal(member.getVoiceState().getChannel());
        if (!bool) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setDescription("何らかのエラーが発生したため、VC名の保存に失敗しました。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":white_check_mark: 保存に成功しました！")
            .setDescription("VC名を保存しました。\n現在のVC名をデフォルトとして使用します。")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }

    void saveall(Guild guild, Member member, SlashCommandInteractionEvent event) {
        if (!member.getId().equals("492088741167366144") && !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":no_pedestrians: 実行する権限がありません！")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
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
        boolean bool = libTitle.saveAsOriginalAll(guild);
        if (!bool) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setDescription("何らかのエラーが発生したため、VC名の保存に失敗しました。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":white_check_mark: 保存に成功しました！")
            .setDescription("タイトル設定中のVCを除き、全てのVC名を保存しました。\n現在のVC名をデフォルトとして使用します。")
            .setColor(LibEmbedColor.success)
            .build()
        ).queue();
    }
}
