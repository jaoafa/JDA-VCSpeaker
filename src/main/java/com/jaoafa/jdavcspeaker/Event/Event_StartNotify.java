package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibFiles;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.MultipleServer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.nio.file.Path;

/**
 * In Guild's all VCs except for the AFK channel, if there is no user except Bot, and someone joins the VC (except for move)
 * <p>
 * If it has been less than one hour since the last notification, no notification will be given.
 */
public class Event_StartNotify extends ListenerAdapter {
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        if (!MultipleServer.isTargetServer(guild)) {
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }
        if (MultipleServer.getVCChannel(guild) == null) {
            return;
        }
        if (guild.getIdLong() != MultipleServer.getVCChannel(guild).getGuild().getIdLong()) {
            return; // テキストチャンネルとGuildが違ったらreturn
        }
        if (MultipleServer.getNotifyChannel(guild) == null) {
            return; // 通知チャンネル(#generalとか)が未定義だったらreturn
        }

        boolean isExistsJoinedChannel = guild.getVoiceChannels().stream()
            .filter(vc -> vc.getGuild().getAfkChannel() != null && // AFKチャンネルが定義されているうえで
                vc.getIdLong() != vc.getGuild().getAfkChannel().getIdLong()) // AFKチャンネル以外であり
            .filter(vc -> vc.getIdLong() != event.getChannelJoined().getIdLong()) // 今回参加されたチャンネル以外であり
            .anyMatch(vc -> vc.getMembers().stream().anyMatch(member -> !member.getUser().isBot())); // Bot以外のユーザーがいるチャンネルがあるか？
        if (isExistsJoinedChannel) {
            return; // 他のVCに誰か人がいる
        }

        long nonBotUsers = event
            .getChannelJoined()
            .getMembers()
            .stream()
            .filter(member -> !member.getUser().isBot())
            .count();

        if (nonBotUsers != 1) {
            return;
        }

        LibFiles.VDirectory.START_NOTIFY_IDS.mkdirs();
        if (LibFiles.VDirectory.START_NOTIFY_IDS.exists(Path.of(guild.getId()))) {
            JSONObject object = LibFiles.VDirectory.START_NOTIFY_IDS.readJSONObject(Path.of(guild.getId()), new JSONObject());

            long lastNotificationTime = object.optLong("lastNotificationTime");
            if (lastNotificationTime >= System.currentTimeMillis() - 3600 * 1000) {
                // 最後の通知が1時間以内
                return;
            }

            String last_notify_id = object.optString("messageId");
            TextChannel channel = MultipleServer.getNotifyChannel(guild);
            if (last_notify_id != null && channel != null) {
                //noinspection ResultOfMethodCallIgnored
                channel.retrieveMessageById(last_notify_id).queue(Message::delete);
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(":inbox_tray: 会話が始まりました！")
            .setDescription("%s が <#%s> に参加しました。".formatted(event.getMember().getAsMention(), event.getChannelJoined().getId()))
            .setColor(LibEmbedColor.normal);
        TextChannel channel = MultipleServer.getNotifyChannel(guild);
        if (channel == null) {
            return;
        }
        channel.sendMessageEmbeds(embed.build()).queue(
            message -> {
                boolean bool = LibFiles.VDirectory.START_NOTIFY_IDS.writeFile(Path.of(guild.getId()), new JSONObject()
                    .put("messageId", message.getId())
                    .put("lastNotificationTime", System.currentTimeMillis()));
                if (!bool) {
                    new LibFlow("startNotify")
                        .error("START_NOTIFY_IDS.writeFile: Failed");
                }
            }
        );
    }
}
