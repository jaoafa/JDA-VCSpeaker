package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.GuildMusicManager;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.Map;
import java.util.stream.Collectors;

public class Cmd_Vcspeaker implements CmdSubstrate {
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
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VCSpeakerをサーバーに設定します")
                    .addSubcommandGroups(
                        new SubcommandGroupData("server", "サーバー設定")
                            .addSubcommands(
                                new SubcommandData("add", "読み上げるチャンネルを設定します")
                                    .addOption(OptionType.CHANNEL, "channel", "設定するチャンネル", false),
                                new SubcommandData("notify", "通知チャンネルを設定します")
                                    .addOption(OptionType.CHANNEL, "channel", "設定するチャンネル", false),
                                new SubcommandData("remove", "サーバーからVCSpeakerの設定を削除します")
                            ),
                        new SubcommandGroupData("debug", "デバッグ用")
                            .addSubcommands(
                                new SubcommandData("queue", "キューを表示します")
                            )
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        switch (subCmd) {
            case "server:add" -> addServer(guild, channel, member, event);
            case "server:notify" -> removeServer(guild, member, event);
            case "server:remove" -> setNotifyChannel(guild, member, event);
            case "debug:queue" -> showDebugQueue(member, event);
        }
    }

    void addServer(Guild guild, GuildMessageChannel channel, Member member, SlashCommandInteractionEvent event) {
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

        OptionMapping channelOpt = event.getOption("channel");
        boolean isSuccessful =
            MultipleServer
                .addServer(
                    guild,
                    channelOpt != null ? channelOpt.getAsChannel().asGuildMessageChannel() : channel
                );

        cmdFlow.success("%s がサーバ登録をリクエストしました: %s", event.getUser().getAsTag(), isSuccessful ? "成功" : "失敗");
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

    void removeServer(Guild guild, Member member, SlashCommandInteractionEvent event) {
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

        cmdFlow.success("%s がサーバ登録解除をリクエストしました: %s", event.getUser().getAsTag(), isSuccessful ? "成功" : "失敗");
        event.replyEmbeds(new EmbedBuilder()
            .setDescription("このサーバの登録解除に%sしました".formatted(isSuccessful ? "成功" : "失敗"))
            .setColor(LibEmbedColor.error)
            .build()
        ).queue();
    }

    void setNotifyChannel(Guild guild, Member member, SlashCommandInteractionEvent event) {
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
                    Main.getExistsOption(event, "channel")
                        .getAsChannel().asGuildMessageChannel()
                );

        cmdFlow.success("%s が通知チャンネルの設定をリクエストしました: %s", event.getUser().getAsTag(), isSuccessful ? "成功" : "失敗");
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

    void showDebugQueue(Member member, SlashCommandInteractionEvent event) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(NO_PERMISSION.build()).queue();
            return;
        }
        Map<Long, GuildMusicManager> managers = PlayerManager.getMusicManagers();
        if (managers.isEmpty()) {
            event.reply("empty managers").queue();
            return;
        }
        event.reply(managers.entrySet().stream().map(entry -> {
            TrackScheduler scheduler = entry.getValue().scheduler;
            return entry.getKey() + ": " + scheduler.getQueue().size() + " (isPaused: " + scheduler.player.isPaused() + ")";
        }).collect(Collectors.joining("\n"))).queue();
    }
}

