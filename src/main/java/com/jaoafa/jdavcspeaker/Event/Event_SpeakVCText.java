package com.jaoafa.jdavcspeaker.Event;

import com.jaoafa.jdavcspeaker.Lib.*;
import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.MessageProcessor.BaseProcessor;
import com.jaoafa.jdavcspeaker.MessageProcessor.ProcessorType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class Event_SpeakVCText extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }
        Guild guild = event.getGuild();
        if (!MultipleServer.isTargetServer(guild)) {
            return;
        }
        final JDA jda = event.getJDA();
        final TextChannel channel = event.getChannel().asTextChannel();
        final Message message = event.getMessage();
        if (channel.getIdLong() != MultipleServer.getVCChannelId(guild)) {
            return; // VCテキストチャンネル以外からのメッセージ
        }
        final Member member = event.getMember();
        if (member == null) {
            return;
        }

        User user = event.getAuthor();
        if (user.getIdLong() == jda.getSelfUser().getIdLong()) {
            return;
        }

        final String content = message.getContentDisplay(); // チャンネル名・リプライとかが表示通りになっている文字列が返る
        if (content.equals(".")) {
            return; // .のみは除外
        }
        if (guild.getSelfMember().getVoiceState() == null ||
            guild.getSelfMember().getVoiceState().getChannel() == null) {
            // 自身がどこにも入っていない場合

            if (!Main.getArgs().isDisableAutoJoinByMessage &&
                member.getVoiceState() != null &&
                member.getVoiceState().getChannel() != null) {
                // メッセージ送信者がどこかのVCに入っている場合（機能が有効な場合に限る）

                guild.getAudioManager().openAudioConnection(member.getVoiceState().getChannel()); // 参加
                if (MultipleServer.getVCChannel(guild) != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(":white_check_mark: AutoJoined")
                        .setDescription("`" + member.getVoiceState().getChannel().getName() + "`へ自動接続しました。")
                        .setColor(LibEmbedColor.success);
                    MultipleServer.getVCChannel(guild).sendMessageEmbeds(embed.build()).queue();
                }
            } else {
                return; // 自身がどこにも入っておらず、送信者もどこにも入っていない場合
            }
        }

        UserVoiceTextResult uvtr = UserVoiceTextResult.getUserVoiceText(user);
        if (uvtr.isReset()) {
            message.reply("デフォルトパラメーターが不正であるため、リセットしました。").queue();
        }

        List<ProcessorType> processorTypes = ProcessorType.getMatchProcessor(message);
        for (BaseProcessor processor : getMessageProcessors()) {
            if (!processorTypes.contains(processor.getType())) {
                continue;
            }

            processor.execute(
                jda,
                guild,
                channel,
                member,
                message,
                uvtr
            );
        }
    }

    List<BaseProcessor> getMessageProcessors() {
        List<BaseProcessor> list = new ArrayList<>();
        try {
            for (Class<?> eventClass : new LibClassFinder().findClasses("com.jaoafa.jdavcspeaker.MessageProcessor")) {
                if (eventClass.isInterface() ||
                    !eventClass.getSimpleName().endsWith("Processor")
                    || eventClass.getEnclosingClass() != null
                    || eventClass.getName().contains("$")) {
                    continue;
                }
                Object instance = ((Constructor<?>) eventClass.getConstructor()).newInstance();
                if (!(instance instanceof BaseProcessor)) {
                    continue;
                }

                list.add((BaseProcessor) eventClass.getConstructor().newInstance());
            }
        } catch (Exception e) {
            new LibReporter(null, e);
        }
        return list;
    }
}
