package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * When someone leaves the VC, notify the VC text channel.
 */
public class Event_Disconnect extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (Main.getArgs().isDisableUserActivityNotify) {
            return;
        }
        if (!MultipleServer.isTargetServer(event.getGuild())) {
            return;
        }
        AudioChannel vc = event.getChannelLeft();

        LibTitle libTitle = Main.getLibTitle();
        if (libTitle != null) {
            libTitle.processLeftTitle(vc);
        }

        User user = event.getMember().getUser();
        AudioChannel channel = event.getChannelLeft();
        if (MultipleServer.getVCChannel(event.getGuild()) == null) return;

        String defaultContent = ":outbox_tray: `%s` が <#%s> から退出しました。".formatted(
            user.getName(),
            channel.getId());
        Message message = MultipleServer
            .getVCChannel(event.getGuild())
            .sendMessage("%s移動先を探しています…。".formatted(defaultContent))
            .complete();

        if (!event.getMember().getUser().isBot()) {
            new VoiceText().play(
                TrackInfo.SpeakFromType.QUITED_VC,
                message,
                "%s が %s から退出しました。".formatted(
                    user.getName(),
                    MsgFormatter.formatChannelName(channel.getName()))
            );
        }

        new Thread(() -> {
            try {
                JSONArray destinationChannels = getDestinationChannels(user.getId());
                System.out.println(destinationChannels);
                if (destinationChannels != null &&
                    destinationChannels.length() > 0 &&
                    !destinationChannels.getJSONObject(0).getString("guildId").equals(event.getGuild().getId())) {
                    JSONObject destinationChannel = destinationChannels.getJSONObject(0);
                    message.editMessage(":outbox_tray: `%s` が <#%s> から退出し、%s の %s に移動しました。".formatted(
                        user.getName(),
                        channel.getId(),
                        destinationChannel.getString("guildName"),
                        destinationChannel.getString("channelName"))).queue();

                    if (!event.getMember().getUser().isBot()) {
                        new VoiceText().play(
                            TrackInfo.SpeakFromType.QUITED_VC,
                            message,
                            "%s は %s の %s へ移動しました。".formatted(
                                user.getName(),
                                destinationChannel.getString("guildName"),
                                MsgFormatter.formatChannelName(destinationChannel.getString("channelName")))
                        );
                    }
                } else {
                    message.editMessage(defaultContent).queue();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JSONArray getDestinationChannels(String userId) throws IOException {
        Process p;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(List.of("node", "--no-warnings", LibFiles.VFile.EXTERNAL_SCRIPT_DESTINATION_CHANNEL.getPath().toString(), "--userId", userId));
            builder.redirectErrorStream(true);
            builder.directory(new File("."));
            p = builder.start();
            boolean bool = p.waitFor(3, TimeUnit.MINUTES);
            if (!bool) {
                return null;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(p.getInputStream());
            Stream<String> streamOfString = new BufferedReader(inputStreamReader).lines();
            String streamToString = streamOfString.collect(Collectors.joining("\n"));
            if (p.exitValue() != 0) {
                System.out.println(p.exitValue());
                System.out.println(streamToString);
                return null;
            }
            return new JSONArray(streamToString);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
