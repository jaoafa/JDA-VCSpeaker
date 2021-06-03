package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.DefaultParamsManager;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Default implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .literal("user")
                        .argument(StringArgument.<JDACommandSender>newBuilder("params").greedy())
                        .handler(context -> execute(context, this::setUser))
                        .build(),
                builder
                        .literal("user")
                        .literal("get")
                        .handler(context -> execute(context, this::getUser))
                        .build(),
                builder
                        .literal("user")
                        .literal("reset")
                        .handler(context -> execute(context, this::resetUser))
                        .build(),
                builder
                        .argument(StringArgument.<JDACommandSender>newBuilder("params").greedy())
                        .handler(context -> execute(context, this::setUser))
                        .build(),
                builder
                        .literal("get")
                        .handler(context -> execute(context, this::getUser))
                        .build(),
                builder
                        .literal("reset")
                        .handler(context -> execute(context, this::resetUser))
                        .build()
        );
    }

    void setUser(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        String params = context.getOrDefault("params", "");
        assert params != null;
        User user = member.getUser();
        VoiceText vt;
        try {
            vt = new VoiceText().parseMessage(params);
        } catch (VoiceText.WrongSpeakerException e) {
            String allowParams = Arrays.stream(VoiceText.Speaker.values())
                                       .filter(s -> !s.equals(VoiceText.Speaker.__WRONG__))
                                       .map(Enum::name)
                                       .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                    .setTitle(":bangbang: メッセージパラメーターが不正")
                    .setDescription(String.format("`speaker` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
            return;
        } catch (VoiceText.WrongSpeedException e) {
            message.reply(new EmbedBuilder()
                    .setTitle(":bangbang: メッセージパラメーターが不正")
                    .setDescription(String.format("`speed` が正しくありません。使用可能なパラメーターは `%s` です。", "50 ～ 400"))
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
            return;
        } catch (VoiceText.WrongEmotionException e) {
            String allowParams = Arrays.stream(VoiceText.Emotion.values())
                                       .filter(s -> !s.equals(VoiceText.Emotion.__WRONG__))
                                       .map(Enum::name)
                                       .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                    .setTitle(":bangbang: メッセージパラメーターが不正")
                    .setDescription(String.format("`emotion` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
            return;
        } catch (VoiceText.WrongEmotionLevelException e) {
            String allowParams = Arrays.stream(VoiceText.EmotionLevel.values())
                                       .filter(s -> !s.equals(VoiceText.EmotionLevel.__WRONG__))
                                       .map(Enum::name)
                                       .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                    .setTitle(":bangbang: メッセージパラメーターが不正")
                    .setDescription(String.format("`emotionLevel` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
            return;
        } catch (VoiceText.WrongPitchException e) {
            message.reply(new EmbedBuilder()
                    .setTitle(":bangbang: メッセージパラメーターが不正")
                    .setDescription(String.format("`pitch` が正しくありません。使用可能なパラメーターは `%s` です。", "50 ～ 200"))
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
            return;
        }

        boolean bool;
        bool = new DefaultParamsManager(user).setDefaultVoiceText(vt);
        if (bool) {
            message.reply(new EmbedBuilder()
                    .setTitle(":joystick: ユーザーデフォルトパラメータ")
                    .setDescription("デフォルトパラメータの設定保存に成功しました。")
                    .setColor(LibEmbedColor.success)
                    .build()).queue();
        } else {
            message.reply(new EmbedBuilder()
                    .setTitle(":joystick: ユーザーデフォルトパラメータ")
                    .setDescription("デフォルトパラメータの設定保存に失敗しました。")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
        }
    }

    void getUser(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        User user = member.getUser();
        try {
            VoiceText vt = new DefaultParamsManager(user).getDefaultVoiceText();
            if (vt == null) {
                message.reply(new EmbedBuilder()
                        .setTitle(":joystick: ユーザーデフォルトパラメータ")
                        .setDescription("あなたのデフォルトパラメーターは現在設定されていません。`" + Main.getPrefix() + "default user <Params>` で指定できます。")
                        .setColor(LibEmbedColor.cation)
                        .build()).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(":joystick: ユーザーデフォルトパラメータ")
                    .setDescription("現在設定されているあなたのデフォルトパラメーターは以下の通りです。")
                    .setColor(LibEmbedColor.success);
            embed.addField("Speaker", String.format("`%s`", vt.getSpeaker().name().toLowerCase()), true);
            embed.addField("Speed", String.format("`%d`", vt.getSpeed()), true);
            embed.addField("Emotion", vt.getEmotion() != null ?
                    String.format("`%s`", vt.getEmotion().name().toLowerCase()) :
                    "未設定", true);
            embed.addField("Emotion level", String.format("`%s` (`%d`)", vt.getEmotionLevel().name().toLowerCase(), vt.getEmotionLevel().getLevel()), true);
            embed.addField("Pitch", String.format("`%d`", vt.getPitch()), true);
            message.reply(embed.build()).queue();
        } catch (VoiceText.WrongException e) {
            message.reply(new EmbedBuilder()
                    .setTitle(":joystick: ユーザーデフォルトパラメータ")
                    .setDescription("デフォルトパラメータの設定取得に失敗しました。正しくない値が指定されています。")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
        }
    }

    void resetUser(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        User user = member.getUser();
        boolean bool = new DefaultParamsManager(user).setDefaultVoiceText(null);
        message.reply(new EmbedBuilder()
                .setTitle(":joystick: ユーザーデフォルトパラメータ")
                .setDescription("デフォルトパラメータのリセットに" + (bool ? "成功" : "失敗") + "しました。")
                .setColor(bool ? LibEmbedColor.success : LibEmbedColor.error)
                .build()).queue();
    }
}
