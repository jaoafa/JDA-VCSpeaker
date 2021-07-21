package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.DefaultParamsManager;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Cmd_Default implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":expressionless:")
            .setData(
                new CommandData(this.getClass().getSimpleName().substring(4).toLowerCase(), "ユーザーのデフォルト音声を設定します")
                    .addSubcommands(
                        new SubcommandData("get", "現在の音声パラメーターを取得"),
                        new SubcommandData("set", "デフォルトパラメーターを設定")
                            .addOption(OptionType.STRING, "params", "設定するパラメーター", true),
                        new SubcommandData("reset", "デフォルトパラメーターをリセット")
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       MessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandEvent event, String subCmd) {
        switch (subCmd) {
            case "get" -> getUser(user, event);
            case "set" -> setUser(user, event);
            case "reset" -> resetUser(user, event);
        }
    }

    void setUser(User user, SlashCommandEvent event) {
        String params = event.getOption("params").getAsString(/*絶対100%確実にRequired*/);
        VoiceText vt;
        try {
            vt = new VoiceText().parseMessage(params);
        } catch (VoiceText.WrongSpeakerException e) {
            String allowParams = Arrays.stream(VoiceText.Speaker.values())
                .filter(s -> !s.equals(VoiceText.Speaker.__WRONG__))
                .map(Enum::name)
                .collect(Collectors.joining("`, `"));
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`speaker` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (VoiceText.WrongSpeedException e) {
            event.replyEmbeds(new EmbedBuilder()
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
            event.replyEmbeds(new EmbedBuilder()
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
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`emotionLevel` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (VoiceText.WrongPitchException e) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`pitch` が正しくありません。使用可能なパラメーターは `%s` です。", "50 ～ 200"))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }

        boolean isSuccessful = new DefaultParamsManager(user).setDefaultVoiceText(vt);
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":joystick: ユーザーデフォルトパラメータ")
            .setDescription("デフォルトパラメータの設定保存に%sしました。".formatted(isSuccessful ? "成功" : "失敗"))
            .setColor(isSuccessful ? LibEmbedColor.success : LibEmbedColor.error)
            .build()
        ).queue();
    }

    void getUser(User user, SlashCommandEvent event) {
        try {
            VoiceText vt = new DefaultParamsManager(user).getDefaultVoiceText();
            if (vt == null) {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":joystick: ユーザーデフォルトパラメータ")
                    .setDescription("あなたのデフォルトパラメーターは現在設定されていません。`" + Main.getPrefix() + "default user <Params>` で指定できます。")
                    .setColor(LibEmbedColor.cation)
                    .build()).queue();
                return;
            }
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":joystick: ユーザーデフォルトパラメータ")
                .setDescription("現在設定されているあなたのデフォルトパラメーターは以下の通りです。")
                .setColor(LibEmbedColor.success)
                .addField("Speaker", String.format("`%s`", vt.getSpeaker().name().toLowerCase()), true)
                .addField("Speed", String.format("`%d`", vt.getSpeed()), true)
                .addField("Emotion", vt.getEmotion() != null ?
                    String.format("`%s`", vt.getEmotion().name().toLowerCase()) :
                    "未設定", true)
                .addField("Emotion level", String.format("`%s` (`%d`)", vt.getEmotionLevel().name().toLowerCase(), vt.getEmotionLevel().getLevel()), true)
                .addField("Pitch", String.format("`%d`", vt.getPitch()), true)
                .build()
            ).queue();
        } catch (VoiceText.WrongException e) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":joystick: ユーザーデフォルトパラメータ")
                .setDescription("デフォルトパラメータの設定取得に失敗しました。正しくない値が指定されています。")
                .setColor(LibEmbedColor.error)
                .build()).queue();
        }
    }

    void resetUser(User user, SlashCommandEvent event) {
        boolean isSuccessful = new DefaultParamsManager(user).setDefaultVoiceText(null);
        event.replyEmbeds(new EmbedBuilder()
            .setTitle(":joystick: ユーザーデフォルトパラメータ")
            .setDescription("デフォルトパラメータのリセットに" + (isSuccessful ? "成功" : "失敗") + "しました。")
            .setColor(isSuccessful ? LibEmbedColor.success : LibEmbedColor.error)
            .build()
        ).queue();
    }
}
