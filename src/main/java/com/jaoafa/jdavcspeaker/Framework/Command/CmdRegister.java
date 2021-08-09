package com.jaoafa.jdavcspeaker.Framework.Command;

import com.jaoafa.jdavcspeaker.Lib.LibClassFinder;
import com.jaoafa.jdavcspeaker.Lib.LibFlow;
import com.jaoafa.jdavcspeaker.Lib.LibReporter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;

public class CmdRegister {
    public CmdRegister(JDA jda) {
        new LibFlow().header("PublicCommand").setName("PublicCmd").run();
        ArrayList<CommandData> commandList = new ArrayList<>();
        try {
            for (Class<?> cmdClass : new LibClassFinder().findClasses("com.jaoafa.jdavcspeaker.Command")) {
                if (!cmdClass.getSimpleName().startsWith("Cmd_")
                    || cmdClass.getEnclosingClass() != null
                    || cmdClass.getName().contains("$")) {
                    new LibFlow().error("%sはCommandクラスではありません。スキップします...", cmdClass.getSimpleName()).run();
                    continue;
                }
                CmdSubstrate cmd = (CmdSubstrate) cmdClass.getConstructor().newInstance();
                commandList.add(cmd.detail().getData());
                new LibFlow().success("%sを登録キューに挿入しました。", cmdClass.getSimpleName()).run();
            }
        } catch (Exception e) {
            new LibReporter(null, e);
        }

        //全てのサーバーで登録
        for (Guild guild : jda.getGuilds()) {
            guild.updateCommands().addCommands(commandList).complete();
            new LibFlow().success("%sへの登録に成功しました。", guild.getName()).run();
        }
        new LibFlow().success("全てのGuildに登録しました。").run();
    }
}
