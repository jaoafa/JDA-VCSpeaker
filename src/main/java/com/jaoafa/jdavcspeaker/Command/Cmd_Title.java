package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.EmojiWrapper;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import com.jaoafa.jdavcspeaker.Lib.VoiceText;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class Cmd_Title implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":regional_indicator_t:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "参加中VCにタイトルを設定します")
                    .addOption(OptionType.STRING, "title", "設定するタイトル", true)
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        title(member, event);
    }

    void title(Member member, SlashCommandInteractionEvent event) {
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
        boolean old_title_is_first_emoji = EmojiWrapper.removeAllEmojis(old_title).length() > 0 && !old_title.substring(0, 1).equals(EmojiWrapper.removeAllEmojis(old_title).substring(0, 1));
        /* 新VC名の一文字目が絵文字であるかどうか (旧VC名から絵文字を消したときに0文字だったら false) */
        boolean new_title_is_first_emoji = EmojiWrapper.removeAllEmojis(new_title).length() > 0 && !new_title.substring(0, 1).equals(EmojiWrapper.removeAllEmojis(new_title).substring(0, 1));

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
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":x: 保存に失敗しました。")
                .setColor(LibEmbedColor.error);
            if (result.message() != null) {
                try {
                    JSONObject json = new JSONObject(result.message());
                    embed.setDescription(getErrorMessage(member, json));
                } catch (JSONException e) {
                    embed.setDescription(result.message());
                }
            }
            event.replyEmbeds(embed.build()).queue();
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
                    String.format("タイトルを %s に変更しました", title)
                )
            )
        );
    }

    String getErrorMessage(Member member, JSONObject json) {
        if (!json.has("message")) {
            return json.toString();
        }
        List<Long> targetUserIds = List.of(
            206692134991036416L, // Zakuro
            221991565567066112L, // Tomachi
            189372008147058688L, // Zokasu
            189377054955798528L, // Ekusas
            216206763102437376L, // Kaepi
            239608383261507584L, // Ohamu
            372701608053833730L // Eno
        );
        List<String> rateLimitTemplates = List.of(
            "レートリミットつってさー、ディスコにキレられたんよー。\n{} 後にさー、もっかいやってくんね？",
            "Failed to change title due to rate limit limitation; can only change twice in 10 minutes; please try again after {}.",
            "Impossibile cambiare il titolo a causa delle restrizioni del limite di velocità; può essere cambiato solo due volte in 10 minuti; riprova dopo {}.",
            "Tiitli muutmine ebaõnnestus kiiruse piirangute tõttu; 10 minuti jooksul saab muuta ainult kaks korda; proovige uuesti {} pärast.",
            "Titel kon niet worden gewijzigd wegens beperkingen van de tarieflimiet; kan slechts tweemaal in 10 minuten worden gewijzigd; probeer het opnieuw na {}.",
            "Απέτυχε η αλλαγή τίτλου λόγω περιορισμών στο όριο ρυθμού- μπορεί να αλλάξει μόνο δύο φορές μέσα σε 10 λεπτά- προσπαθήστε ξανά μετά από {}.",
            "Det gick inte att ändra titeln på grund av begränsningar i hastighetsgränsen; kan endast ändras två gånger på 10 minuter; försök igen efter {}.",
            "No se ha podido cambiar el título debido a las restricciones del límite de velocidad; sólo se puede cambiar dos veces en 10 minutos; inténtelo de nuevo después de {}.",
            "Nepodarilo sa zmeniť názov z dôvodu obmedzenia rýchlosti; je možné ho zmeniť len dvakrát za 10 minút; skúste to znova po {}.",
            "Otsikon vaihtaminen epäonnistui nopeusrajoitusten vuoksi; otsikkoa voi vaihtaa vain kahdesti 10 minuutin aikana; yritä uudelleen {} kuluttua.",
            "Не успяхте да промените заглавието поради ограничения на тарифата; може да се промени само два пъти в рамките на 10 минути; опитайте отново след {}.",
            "Не удалось изменить название из-за ограничений лимита тарифов; можно изменить только дважды за 10 минут; повторите попытку через {}.",
            "由于速率限制，更改标题失败；10分钟内只能更改两次；{}钟后再试。"
        );

        String message = json.getString("message");
        if (message.equals("The resource is being rate limited.")) {
            String retryAfter = (json.getInt("retry_after") / 1000 / 60) + "分" + (json.getInt("retry_after") / 1000 % 60) + "秒 (" + json.getInt("retry_after") + "ms)";
            if (targetUserIds.contains(member.getIdLong())) {
                return rateLimitTemplates.get(new Random().nextInt(rateLimitTemplates.size())).replace("{}", retryAfter);
            }
            return "レートリミットエラーです。\n\nDiscordの内部レート制限により、ボイスチャンネルのチャンネル名変更は **10分に2回まで** に制限されています。\n" + retryAfter + " 後に再度実行してください。";
        }
        if (message.equals("Missing Permissions")) {
            return "権限エラーです。\n\nボイスチャンネルのチャンネル名変更には **チャンネルの管理** の権限が必要です。運営にお問い合わせください。";
        }
        return message;
    }
}
