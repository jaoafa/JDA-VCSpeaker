package com.jaoafa.jdavcspeaker;

import com.jaoafa.jdavcspeaker.Command.CmdHook;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.jaoafa.jdavcspeaker.Util.Logger;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) throws Exception{
        try {
            Logger.print("VCSpeaker Starting...");
            JDABuilder builder = JDABuilder.createDefault(JSONUtil.read("./VCSpeaker.json").getString("DiscordToken"));
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS,GatewayIntent.GUILD_PRESENCES,GatewayIntent.GUILD_MESSAGES);
            builder.setEventManager(new AnnotatedEventManager());
            builder.addEventListeners(new CmdHook());
            builder.build().awaitReady();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}