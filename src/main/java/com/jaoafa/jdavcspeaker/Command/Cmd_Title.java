package com.jaoafa.jdavcspeaker.Command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.CmdBuilders;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.Lib.LibTitle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import static com.jaoafa.jdavcspeaker.Command.CmdExecutor.execute;

public class Cmd_Title implements CmdInterface {
    @Override
    public CmdBuilders register(Command.Builder<JDACommandSender> builder) {
        return new CmdBuilders(
                builder
                        .handler(context -> execute(context, this::title))
                        .argument(StringArgument.of("title"))
                        .build()
        );
    }

    void title(Guild guild, MessageChannel channel, Member member, Message message, CommandContext<JDACommandSender> context) {
        if (member.getVoiceState().getChannel() == null){
            message.reply(new EmbedBuilder()
                    .setTitle(":no_entry_sign: VCに入ってから実行してください")
                    .setColor(LibEmbedColor.error)
                    .build()
            ).queue();
            return;
        }
        boolean profileExists = LibTitle.setTitle(member.getVoiceState().getChannel(), context.get("title"));
        if (!profileExists){
            LibTitle.saveAsOriginal(member.getVoiceState().getChannel());
            LibTitle.setTitle(member.getVoiceState().getChannel(), context.get("title"));
        }
        message.reply(new EmbedBuilder()
                .setTitle(":magic_wand: タイトルを変更しました！")
                .setDescription(String.format("`%s`\n全員退出したらリセットされます。", context.get("title")))
                .setColor(LibEmbedColor.success)
                .build()
        ).queue();
    }
}
