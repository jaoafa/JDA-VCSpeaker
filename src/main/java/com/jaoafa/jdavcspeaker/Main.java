package com.jaoafa.jdavcspeaker;

import com.jaoafa.jdavcspeaker.Command.CmdHook;
import com.jaoafa.jdavcspeaker.Event.*;
import com.jaoafa.jdavcspeaker.Lib.LibJson;
import com.jaoafa.jdavcspeaker.Lib.Logger;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Logger.print("VCSpeaker Starting...");
            JDABuilder builder = JDABuilder.createDefault(LibJson.read("./VCSpeaker.json").getString("DiscordToken"));
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
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
    public void onReady(ReadyEvent event) {
        File newdir = new File("./Temp");
        newdir.mkdir();
        StaticData.jda = event.getJDA();
        System.out.println("VCSPEAKER!!!!!!!!!!!!!!!!!!!!STARTED!!!!!!!!!!!!:tada::tada:");
    }
}