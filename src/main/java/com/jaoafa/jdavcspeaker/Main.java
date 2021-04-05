package com.jaoafa.jdavcspeaker;

import com.jaoafa.jdavcspeaker.Command.CmdHook;
import com.jaoafa.jdavcspeaker.Event.*;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.jaoafa.jdavcspeaker.Util.Logger;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Logger.print("VCSpeaker Starting...");
            JDABuilder builder = JDABuilder.createDefault(JSONUtil.read("./VCSpeaker.json").getString("DiscordToken"));
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES,GatewayIntent.GUILD_VOICE_STATES);
            builder.setEventManager(new AnnotatedEventManager());

            builder.addEventListeners(new CmdHook());
            builder.addEventListeners(new Main());

            builder.addEventListeners(new AutoSummon());
            builder.addEventListeners(new AutoMove());
            builder.addEventListeners(new AutoDisconnect());

            builder.addEventListeners(new Event_Join());
            builder.addEventListeners(new Event_Move());
            builder.addEventListeners(new Event_Disconnect());
            builder.addEventListeners(new Event_SpeakVCText());
            builder.build().awaitReady();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onReady(ReadyEvent event){
        StaticData.jda = event.getJDA();
        System.out.println("VCSPEAKER!!!!!!!!!!!!!!!!!!!!STARTED!!!!!!!!!!!!:tada::tada:");
    }
}