package com.jaoafa.jdavcspeaker;

import com.jaoafa.jdavcspeaker.Command.CmdHook;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.jaoafa.jdavcspeaker.Util.Logger;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
    public static void main(String[] args) throws Exception{
        Logger.print("VCSpeaker Starting...");
        JDABuilder builder = JDABuilder.createDefault(JSONUtil.read("./VCSpeaker.json").getString("DiscordToken"));
        builder.addEventListeners(new CmdHook());
        builder.build().awaitReady();
    }
}