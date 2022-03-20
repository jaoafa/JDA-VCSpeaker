package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

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
        AudioChannel targetVC = member.getVoiceState().getChannel();

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

        String old_title = targetVC.getName();
        /* 旧VC名の一文字目が絵文字であるかどうか (旧VC名から絵文字を消したときに0文字だったら false) */
        boolean old_title_is_first_emoji = EmojiParser.removeAllEmojis(old_title).length() > 0 && !old_title.substring(0, 1).equals(EmojiParser.removeAllEmojis(old_title).substring(0, 1));
        /* 新VC名の一文字目が絵文字であるかどうか (旧VC名から絵文字を消したときに0文字だったら false) */
        boolean new_title_is_first_emoji = EmojiParser.removeAllEmojis(new_title).length() > 0 && !new_title.substring(0, 1).equals(EmojiParser.removeAllEmojis(new_title).substring(0, 1));

        List<String> old_title_emojis = EmojiParser.extractEmojis(old_title);
        if (old_title_is_first_emoji && !new_title_is_first_emoji && old_title_emojis.size() > 0) {
            /* 旧VC名の一文字目が絵文字で、新VC名の一文字目が絵文字でない場合 */
            new_title = old_title_emojis.get(0) + new_title;
        }

        boolean isInitialized = false;
        if (!libTitle.existsTitle(targetVC)) {
            isInitialized = libTitle.saveAsOriginal(targetVC);
        }
        LibTitle.ChannelTitleChangeResponse result = libTitle.setTitle(member.getVoiceState().getChannel(), new_title);
        if (!result.result()) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setDescription("エラーが発生: `%s`".formatted(result.message()))
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }
        cmdFlow.success("%s がChannel ID: %s のVC名を変更しました: %s -> %s", event.getUser().getAsTag(), member.getVoiceState().getChannel().getId(), old_title, new_title);

        String title = new_title;
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
                origin_msg -> new VoiceText().play(
                    TrackInfo.SpeakFromType.CHANGED_TITLE,
                    origin_msg,
                    String.format("タイトルを%sに変更しました", title)
                )
            )
        );
    }
}
