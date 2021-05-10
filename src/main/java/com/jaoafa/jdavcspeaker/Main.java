package com.jaoafa.jdavcspeaker;

import cloud.commandframework.Command;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.jda.JDA4CommandManager;
import cloud.commandframework.jda.JDACommandSender;
import cloud.commandframework.jda.JDAGuildSender;
import com.jaoafa.jdavcspeaker.Event.*;
import com.jaoafa.jdavcspeaker.Lib.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Main extends ListenerAdapter {
    static VisionAPI visionAPI = null;
    static String prefix;

    public static void main(String[] args) {
        try {
            Logger.print("VCSpeaker Starting...");
            JSONObject config = LibJson.readObject("./VCSpeaker.json");
            JDABuilder builder = JDABuilder.createDefault(config.getString("DiscordToken"));
            prefix = config.optString("prefix", ";");
            builder.setChunkingFilter(ChunkingFilter.ALL);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES);
            //builder.setEventManager(new AnnotatedEventManager()); // <- こいつが生きているとコマンドが動作しない

            builder.addEventListeners(new Main());

            builder.addEventListeners(new AutoJoin());
            builder.addEventListeners(new AutoMove());
            builder.addEventListeners(new AutoDisconnect());

            builder.addEventListeners(new Event_Join());
            builder.addEventListeners(new Event_Move());
            builder.addEventListeners(new Event_Disconnect());
            builder.addEventListeners(new Event_SpeakVCText());
            builder.addEventListeners(new Event_GeneralNotify());
            JDA jda = builder.build();

            commandRegister(jda);

            if (config.has("visionAPIKey")) {
                visionAPI = new VisionAPI(config.getString("visionAPIKey"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void commandRegister(JDA jda) {
        JSONObject config = LibJson.readObject("./VCSpeaker.json");
        try {
            final JDA4CommandManager<JDACommandSender> manager = new JDA4CommandManager<>(
                jda,
                message -> prefix,
                (sender, perm) -> true,
                CommandExecutionCoordinator.simpleCoordinator(),
                sender -> {
                    MessageReceivedEvent event = sender.getEvent().orElse(null);

                    if (sender instanceof JDAGuildSender) {
                        JDAGuildSender jdaGuildSender = (JDAGuildSender) sender;
                        return new JDAGuildSender(event, jdaGuildSender.getMember(), jdaGuildSender.getTextChannel());
                    }

                    return null;
                    },
                    user -> {
                        MessageReceivedEvent event = user.getEvent().orElse(null);

                        if (user instanceof JDAGuildSender) {
                            JDAGuildSender guildUser = (JDAGuildSender) user;
                            return new JDAGuildSender(event, guildUser.getMember(), guildUser.getTextChannel());
                        }

                        return null;
                    }
                    );

            manager.registerExceptionHandler(NoSuchCommandException.class,
                    (c,e) -> c.getChannel().sendMessage(
                            new EmbedBuilder()
                                    .setTitle(":thinking: コマンドが見つかりませんでした！")
                                    .setColor(LibEmbedColor.error)
                                    .build()
                    ).queue());
            manager.registerExceptionHandler(InvalidSyntaxException.class,
                    (c,e) -> c.getChannel().sendMessage(
                            new EmbedBuilder()
                                    .setTitle(":scroll: コマンドの構文が不正です！")
                                    .setDescription("`"+e.getCorrectSyntax()+"`")
                                    .setColor(LibEmbedColor.error)
                                    .build()
                    ).queue());
            manager.registerExceptionHandler(NoPermissionException.class, (c,e) -> c.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle(":octagonal_sign: 権限がありません！")
                            .setColor(LibEmbedColor.error)
                            .build()
            ).queue());


            ClassFinder classFinder = new ClassFinder();
            for (Class<?> clazz : classFinder.findClasses("com.jaoafa.jdavcspeaker.Command")) {
                if (!clazz.getName().startsWith("com.jaoafa.jdavcspeaker.Command.Cmd_")) {
                    continue;
                }
                if (clazz.getEnclosingClass() != null) {
                    continue;
                }
                if (clazz.getName().contains("$")) {
                    continue;
                }
                String commandName = clazz.getName().substring("com.jaoafa.jdavcspeaker.Command.Cmd_".length())
                        .toLowerCase();

                try {
                    Constructor<?> construct = clazz.getConstructor();
                    Object instance = construct.newInstance();
                    CmdInterface cmdInterface = (CmdInterface) instance;

                    Command.Builder<JDACommandSender> builder = manager.commandBuilder(commandName); // ビルダーを生成
                    cmdInterface.register(builder).getCommands().forEach(manager::command); // manager.command で登録
                    System.out.println(commandName + " register successful");
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println(commandName + " register failed");
                    e.printStackTrace();
                }
            }

            System.out.println(manager.getCommands());
        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        File newdir = new File("./Temp");
        if (!newdir.exists()) {
            boolean bool = newdir.mkdir();
            if (!bool) System.out.println("Failed to create the temporary directory.");
        }
        StaticData.jda = event.getJDA();
        LibAlias.fetchMap();
        System.out.println("VCSPEAKER!!!!!!!!!!!!!!!!!!!!STARTED!!!!!!!!!!!!:tada::tada:");
    }

    @Nullable
    public static VisionAPI getVisionAPI() {
        return visionAPI;
    }

    public static String getPrefix() {
        return prefix;
    }
}