package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.Framework.Command.CmdDetail;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdSubstrate;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibFiles;
import com.jaoafa.jdavcspeaker.Lib.VisionAPI;
import com.jaoafa.jdavcspeaker.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Cmd_Visionapi implements CmdSubstrate {
    @Override
    public CmdDetail detail() {
        return new CmdDetail()
            .setEmoji(":frame_photo:")
            .setData(
                Commands.slash(this.getClass().getSimpleName().substring(4).toLowerCase(), "VisionAPIに関する処理を行います")
                    .addSubcommands(
                        new SubcommandData("getlimit", "現在のVisionAPI利用数を取得します。")
                    )
            );
    }

    @Override
    public void hooker(JDA jda, Guild guild,
                       GuildMessageChannel channel, ChannelType type,
                       Member member, User user,
                       SlashCommandInteractionEvent event, String subCmd) {
        if (subCmd.equals("getlimit")) {
            addServer(guild, channel, member, event);
        }
    }

    void addServer(Guild guild, GuildMessageChannel channel, Member member, SlashCommandInteractionEvent event) {
        VisionAPI visionAPI = Main.getVisionAPI();
        if (visionAPI == null) {
            event.replyEmbeds(new EmbedBuilder()
                .setTitle("VisionAPIが未定義のため、このコマンドは利用できません。")
                .setColor(LibEmbedColor.error)
                .build()
            ).queue();
            return;
        }

        Map<String, Integer> pastMap = visionAPI.getPastRequestCounts();
        String past = pastMap
            .entrySet()
            .stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .reduce("", (a, b) -> a + "\n" + b);
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("VisionAPI")
            .addField("今月利用数", visionAPI.getRequestCount() + " / 950", false)
            .addField("過去利用数", past.length() < 1024 ? past : past.substring(0, 1023), false);

        JSONObject when = LibFiles.VFile.VISION_API_WHEN.readJSONObject(new JSONObject());
        if (when.has(new SimpleDateFormat("yyyy/MM").format(new Date()))) {
            long unixtime_ms = when.getLong(new SimpleDateFormat("yyyy/MM").format(new Date()));
            embed.addField("リミット制限日", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(unixtime_ms)), false);
        }
        event.replyEmbeds(embed.build()).queue();
    }
}
