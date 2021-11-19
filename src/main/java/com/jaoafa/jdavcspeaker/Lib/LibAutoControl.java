package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class LibAutoControl {
    /**
     * メンバーの状態を自動で判定・VCに参加します。
     *
     * @param event  処理するイベント
     * @param member 処理するメンバー
     *
     * @return 処理を続けるかどうか
     */
    public static boolean join(MessageReceivedEvent event, @NotNull Member member) {
        if (event.getGuild().getSelfMember().getVoiceState() == null ||
            event.getGuild().getSelfMember().getVoiceState().getChannel() == null) {
            // 自身がどこにも入っていない場合

            if (member.getVoiceState() != null &&
                member.getVoiceState().getChannel() != null) {
                // メッセージ送信者がどこかのVCに入っている場合

                event.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel()); // 参加
                if (MultipleServer.getVCChannel(event.getGuild()) != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(":white_check_mark: AutoJoined")
                        .setDescription("`" + member.getVoiceState().getChannel().getName() + "`へ自動接続しました。")
                        .setColor(LibEmbedColor.success);
                    MultipleServer.getVCChannel(event.getGuild()).sendMessageEmbeds(embed.build()).queue();
                }
            } else {
                return false; // 自身がどこにも入っておらず、送信者もどこにも入っていない場合
            }
        }
        return true;
    }
}
