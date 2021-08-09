package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Optional;

public class Cmd_VCSpeaker implements CmdSubstrate {
    final EmbedBuilder NO_PERMISSION =
        new EmbedBuilder()
            .setDescription("""
                あなたは管理者権限を所持していないため、
                このサーバでVCSpeakerの設定をすることはできません。
                """)
            .setColor(LibEmbedColor.error);

    //todo 絵文字&説明見直し(ぜんぶ)
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":expressionless:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerをサーバーに設定します")
                    .addSubcommandGroups(
                        new SubcommandGroupData("server", "サーバー設定")
                            .addSubcommands(
                                new SubcommandData("add", "読み上げるチャンネルを設定します")
                                    .addOption(OptionType.CHANNEL, "channel", "設定するチャンネル", false),
                                new SubcommandData("notify", "通知チャンネルを設定します")
                                    .addOption(OptionType.CHANNEL, "channel", "設定するチャンネル", false),
                                new SubcommandData("remove", "サーバーからVCSpeakerの設定を削除します")
                            )
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        switch (subCmd) {
            case "server:add" -> addServer(guild, channel, member, event);
            case "server:notify" -> removeServer(guild, member, event);
            case "server:remove" -> setNotifyChannel(guild, channel, member, event);
        }
    }

    void addServer(Guild guild, MessageChannel channel, Member member, SlashCommandEvent event) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(NO_PERMISSION.build()).queue();
            return;
        }
        if (MultipleServer.isTargetServer(guild)) {
            event.replyEmbeds(new EmbedBuilder()
                .setDescription("""
                    既にこのサーバは登録されており、
                    <#%s> がVCチャンネルとして登録されています。
                    """.formatted(MultipleServer.getVCChannelId(guild)))
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        boolean isSuccessful =
            MultipleServer
                .addServer(
                    guild,
                    Optional.ofNullable(
                        Main.getExistsOption(event, "channel")
                            .getAsMessageChannel()
                    ).orElse(channel)
                );

        event.replyEmbeds(new EmbedBuilder()
            .setTitle("サーバーの登録に%sしました".formatted(isSuccessful ? "成功" : "失敗"))
            .setDescription(
                isSuccessful ?
                    "<#%s>がVCチャンネルとして登録されました".formatted(MultipleServer.getVCChannelId(guild)) :
                    "[この問題を報告する](https://github.com/jaoafa/JDA-VCSpeaker/issues/new)"
            )
            .setColor(isSuccessful ? LibEmbedColor.success : LibEmbedColor.error)
            .build()
        ).queue();
    }

    void removeServer(Guild guild, Member member, SlashCommandEvent event) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(NO_PERMISSION.build()).queue();
            return;
        }
        if (!MultipleServer.isTargetServer(guild)) {
            event.replyEmbeds(new EmbedBuilder()
                .setDescription("まだ登録されていないので、削除もできません！")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        boolean isSuccessful = MultipleServer.removeServer(guild) && MultipleServer.removeNotifyChannel(guild);

        event.replyEmbeds(new EmbedBuilder()
            .setDescription("このサーバの登録解除に%sしました".formatted(isSuccessful ? "成功" : "失敗"))
            .setColor(LibEmbedColor.error)
            .build()
        ).queue();
    }

    void setNotifyChannel(Guild guild, MessageChannel channel, Member member, SlashCommandEvent event) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(NO_PERMISSION.build()).queue();
            return;
        }
        if (MultipleServer.isNotifiable(guild)) {
            event.replyEmbeds(new EmbedBuilder()
                .setDescription("""
                    "既にこのサーバの通知チャンネルは登録されており、
                    <#%s> が通知チャンネルとして登録されています。
                    """.formatted(MultipleServer.getNotifyChannelId(guild)))
                .setColor(LibEmbedColor.cation)
                .build()
            ).queue();
            return;
        }

        boolean isSuccessful =
            MultipleServer
                .setNotifyChannel(guild,
                    Optional.ofNullable(
                        Main.getExistsOption(event, "channel")
                            .getAsMessageChannel()
                    ).orElse(channel)
                );

        event.replyEmbeds(new EmbedBuilder()
            .setTitle("通知チャンネルの登録に%sしました".formatted(isSuccessful ? "成功" : "失敗"))
            .setDescription(
                isSuccessful ?
                    "<#%s>が通知チャンネルとして登録されました。".formatted(MultipleServer.getNotifyChannelId(guild)) :
                    "[この問題を報告する](https://github.com/jaoafa/JDA-VCSpeaker/issues/new)"
            )
            .setColor(isSuccessful ? LibEmbedColor.success : LibEmbedColor.error)
            .build()
        ).queue();
    }
}
