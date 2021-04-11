package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.LibIgnore;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Cmd_Ignore implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        EmbedBuilder eb = new EmbedBuilder();
        if (args[0].equals("add")) {
            if (args[1].equals("contain")||args[1].equals("contains")){
                LibIgnore.addToIgnore("contain", args[2]);
                eb.setTitle(":pencil: 無視項目を設定しました！");
                eb.setDescription(String.format("`%s`が含まれるメッセージは読み上げません。", args[2]));
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            if (args[1].equals("equal")||args[1].equals("equals")){
                LibIgnore.addToIgnore("equal", args[2]);
                eb.setTitle(":pencil: 無視項目を設定しました！");
                eb.setDescription(String.format("`%s`に一致するメッセージは読み上げません。", args[2]));
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            else {
                eb.setTitle(":x: パラメーターがおかしいです！");
                eb.setDescription("第二引数には`contain`,`equal`のどちらかを指定してください。");
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            return;
        }
        if (args[0].equals("remove") || args[0].equals("rm") || args[0].equals("delete") || args[0].equals("del")) {
            if (args[1].equals("contain")||args[1].equals("contains")){
                LibIgnore.removeFromIgnore("contain",args[2]);
                eb.setTitle(":wastebasket: 無視項目を削除しました！");
                eb.setDescription(String.format("今後は`%s`が含まれていたメッセージも読み上げます。", args[2]));
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            if (args[1].equals("equal")||args[1].equals("equals")){
                LibIgnore.removeFromIgnore("equal",args[2]);
                eb.setTitle(":wastebasket: 無視項目を削除しました！");
                eb.setDescription(String.format("今後は`%s`と一致するメッセージも読み上げます。", args[2]));
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            else {
                eb.setTitle(":x: パラメーターがおかしいです！");
                eb.setDescription("第二引数には`contain`,`equal`のどちらかを指定してください。");
                eb.setColor(LibEmbedColor.success);
                channel.sendMessage(eb.build()).queue();
            }
            return;
        }
        if (args[0].equals("list")) {
            final String[] listStr = {""};
            StaticData.ignoreMap.forEach((k, v) -> {
                listStr[0] = listStr[0] + String.format("`%s` : `%s`\n", k, v);
            });
            eb.setTitle(":bookmark_tabs: 現在のエイリアス");
            eb.setDescription(listStr[0]);
            channel.sendMessage(eb.build()).queue();
            return;
        } else {
            eb.setTitle(":x: 引数が見つかりません！");
            eb.setDescription("`add`,`remove`,`list`が利用できます。");
            eb.setColor(LibEmbedColor.cation);
            channel.sendMessage(eb.build()).queue();
        }
    }
}
