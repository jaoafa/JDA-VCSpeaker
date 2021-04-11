package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Lib.LibAlias;
import com.jaoafa.jdavcspeaker.Lib.LibEmbedColor;
import com.jaoafa.jdavcspeaker.StaticData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;

public class Cmd_Alias implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        EmbedBuilder eb = new EmbedBuilder();
        if (args[0].equals("add")){
            LibAlias.addToAlias(args[1],args[2]);
            eb.setTitle(":pencil: エイリアスを設定しました！");
            eb.setDescription(String.format("`%s`を`%s`に置き換えて読み上げます。",args[1],args[2]));
            eb.setColor(LibEmbedColor.success);
            channel.sendMessage(eb.build()).queue();
            return;
        }if (args[0].equals("remove")||args[0].equals("rm")||args[0].equals("delete")||args[0].equals("del")){
            LibAlias.removeFromAlias(args[1]);
            eb.setTitle(":wastebasket: エイリアスを削除しました！");
            eb.setDescription(String.format("`%s`の置き換えを削除しました。",args[1]));
            eb.setColor(LibEmbedColor.success);
            channel.sendMessage(eb.build()).queue();
            return;
        }if (args[0].equals("list")){
            final String[] listStr = {""};
            StaticData.aliasMap.forEach((k,v) ->{
                listStr[0] = listStr[0] + String.format("`%s` -> `%s`\n",k,v);
            });
            eb.setTitle(":bookmark_tabs: 本日のエイリアス");
            eb.setDescription(listStr[0]);
            channel.sendMessage(eb.build()).queue();
        }
    }
}
