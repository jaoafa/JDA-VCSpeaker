package com.jaoafa.jdavcspeaker;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jaoafa.jdavcspeaker.Event.AutoDisconnect;
import com.jaoafa.jdavcspeaker.Event.AutoJoin;
import com.jaoafa.jdavcspeaker.Event.AutoMove;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdRegister;
import com.jaoafa.jdavcspeaker.Framework.Event.EventRegister;
import com.jaoafa.jdavcspeaker.Framework.FunctionHooker;
import com.jaoafa.jdavcspeaker.Lib.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends ListenerAdapter {
    static VisionAPI visionAPI = null;
    static LibTitle libTitle = null;
    static String speakToken = null;
    static final String prefix = "/";

    public static void main(String[] args) {
        new LibFlow()
            .setName("BootStrap")
            .header("VCSpeaker StartUp")
            .action("設定を読み込み中...")
            .run();

        //Task: Config読み込み

        JSONObject config;
        JSONObject tokenConfig;
        try {
            config = LibJson.readObject("./VCSpeaker.json");
        } catch (Exception e) {
            new LibFlow().error("基本設定の読み込みに失敗しました。").run();
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        //Task: Config未定義チェック

        boolean missingConfigDetected = false;
        Function<String, String> missingDetectionMsg = "%sが未定義であるため、初期設定に失敗しました。"::formatted;

        //SubTask: Tokenクラスが無かったら終了
        if (!config.has("Token")) {
            new LibFlow().error(missingDetectionMsg.apply("Tokenクラス")).run();
            System.exit(1);
            return;
        } else {
            tokenConfig = config.getJSONObject("Token");
        }

        //SubTask: DiscordTokenの欠落を検知
        if (!config.getJSONObject("Token").has("Discord")) {
            new LibFlow().error(missingDetectionMsg.apply("DiscordToken")).run();
            missingConfigDetected = true;
        }
        //SubTask: SpeakerTokenの欠落を検知
        if (!config.getJSONObject("Token").has("Speaker")) {
            new LibFlow().error(missingDetectionMsg.apply("SpeakerToken")).run();
            missingConfigDetected = true;
        }

        //SubTask: 終了
        if (missingConfigDetected) {
            System.exit(1);
            return;
        }

        //Task: Token,JDA設定
        speakToken = tokenConfig.getString("Speaker");
        EventWaiter eventWaiter = new EventWaiter();

        JDABuilder builder = JDABuilder.createDefault(tokenConfig.getString("Discord"))
            //JDASettings
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            //Intents
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES
            )
            //EventListeners
            .addEventListeners(new Main())
            .addEventListeners(new FunctionHooker())
            //AutoFunction
            .addEventListeners(new AutoJoin())
            .addEventListeners(new AutoMove())
            .addEventListeners(new AutoDisconnect())
            //EventFunction
            .addEventListeners(eventWaiter);

        //自動登録
        new EventRegister(builder);
        //EventWaiterを記録
        LibValue.eventWaiter = eventWaiter;

        //Task: ログイン
        JDA jda;
        try {
            jda = builder.build().awaitReady();
        } catch (InterruptedException | LoginException e) {
            new LibFlow().error("Discordへのログインに失敗しました。").run();
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        new CmdRegister(jda);

        if (tokenConfig.has("VisionAPI")) {
            try {
                visionAPI = new VisionAPI(tokenConfig.getString("VisionAPI"));
            } catch (Exception e) {
                new LibFlow().error("VisionAPIの初期化に失敗しました。関連機能は動作しません。").run();
                new LibReporter(null, e);
                visionAPI = null;
            }
        }

        try {
            libTitle = new LibTitle("./title.json");
        } catch (Exception e) {
            new LibFlow().error("タイトル設定の読み込みに失敗しました。関連機能は動作しません。").run();
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        //Task: 一時ファイル消去
        if (new File("tmp").exists()) {
            try (Stream<Path> walk = Files.walk(new File("tmp").toPath(), FileVisitOption.FOLLOW_LINKS)) {
                List<File> missDeletes = walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(f -> !f.delete())
                    .collect(Collectors.toList());
                if (missDeletes.size() != 0) {
                    System.out.println("Failed to delete " + missDeletes.size() + " temporary files.");
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        //Task: Devのコマンド削除
        try {
            JDA devJda = JDABuilder.createDefault(tokenConfig.getString("VCSDev")).build().awaitReady();
            devJda.getGuilds().forEach(
                guild -> guild.retrieveCommands().queue(
                    cmds -> cmds.forEach(cmd -> cmd.delete().queue(
                        unused -> System.out.printf("%sで%sが削除されました。%n", guild.getId(),cmd.getName())
                    ))
                )
            );

            new Timer(false).schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        devJda.shutdown();
                    }
                },
                180000
            );
        } catch (InterruptedException | LoginException e) {
            new LibReporter(null,e);
        }
    }

    @Nullable
    public static VisionAPI getVisionAPI() {
        return visionAPI;
    }

    public static String getPrefix() {
        return prefix;
    }

    @Nullable
    public static LibTitle getLibTitle() {
        return libTitle;
    }

    public static String getSpeakToken() {
        return speakToken;
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
        LibIgnore.fetchMap();
        System.out.println("VCSPEAKER!!!!!!!!!!!!!!!!!!!!STARTED!!!!!!!!!!!!:tada::tada:");
    }

    @NotNull
    public static OptionMapping getExistsOption(SlashCommandEvent event, String name) {
        OptionMapping option = event.getOption(name);
        if (option == null) {
            throw new IllegalArgumentException();
        }
        return option;
    }
}