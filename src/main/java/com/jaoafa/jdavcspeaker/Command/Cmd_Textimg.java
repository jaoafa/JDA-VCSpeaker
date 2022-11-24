package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cmd_Textimg implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":a:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "画像からテキストを抽出し、テキスト入り画像を生成します。")
                    .addOption(OptionType.STRING, "messagelink", "画像のメッセージリンク、または画像リンク")
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        generateTextImg(event);
    }

    void generateTextImg(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            event.reply(":x:").queue();
            return;
        }
        String url = Main.getExistsOption(event, "messagelink").getAsString();
        Pattern msgUrlPattern = Pattern.compile("^https://discord(?:app)?\\.com/channels/(\\d+)/(\\d+)/(\\d+)$");
        Pattern mediaUrlPattern = Pattern.compile("^https://(?:cdn|media)\\.discordapp\\.(?:com|net)/attachments/(\\d+)/(\\d+)/(.+)$");

        String imageUrl;
        Matcher msgUrlMatcher = msgUrlPattern.matcher(url);
        if (msgUrlMatcher.matches()) {
            // message url
            String channelId = msgUrlMatcher.group(2);
            String messageId = msgUrlMatcher.group(3);

            if (!channelId.equals(String.valueOf(MultipleServer.getVCChannelId(event.getGuild())))) {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: VCチャンネルのメッセージURLではありません")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
                return;
            }
            Message message = MultipleServer.getVCChannel(event.getGuild()).retrieveMessageById(messageId).complete();
            if (message == null) {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: メッセージが見つかりませんでした")
                    .setDescription("指定されたメッセージURLのメッセージが見つかりませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
                return;
            }
            if (message.getAttachments().size() == 0) {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: 添付ファイルが見つかりませんでした")
                    .setDescription("指定されたメッセージには添付ファイルがありませんでした。")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
                return;
            }
            Message.Attachment attachment = message.getAttachments().get(0);
            String contentType = attachment.getContentType();
            if (!VisionAPI.getSupportedContentType().contains(contentType)) {
                event.replyEmbeds(new EmbedBuilder()
                    .setTitle(":x: 添付ファイルはサポートされていません")
                    .setDescription("指定されたメッセージの添付ファイルは画像ではないようです。")
                    .setColor(LibEmbedColor.error)
                    .build()).queue();
                return;
            }
            imageUrl = attachment.getUrl();
        } else if (mediaUrlPattern.matcher(url).matches()) {
            // media url
            imageUrl = url;
        } else {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle(":x: サポートされていないURLです。")
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }

        event.deferReply(false).queue();

        File file;
        try {
            file = LibTextImg.getTempimgPath(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            event.getHook().editOriginalEmbeds(new EmbedBuilder()
                .setTitle(":x: 画像の生成に失敗しました。")
                .setDescription(e.getMessage())
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }
        if (file == null) {
            event.getHook().editOriginalEmbeds(new EmbedBuilder()
                .setTitle(":x: 画像の生成に失敗しました。(file == null)")
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }
        new LibFlow("textimg").success("File: %s", file.getAbsolutePath());

        event.getHook().editOriginal(file, "output.png").queue();
    }
}