package com.jaoafa.jdavcspeaker;

import com.jaoafa.jdavcspeaker.Event.AutoDisconnect;
import com.jaoafa.jdavcspeaker.Event.AutoJoin;
import com.jaoafa.jdavcspeaker.Event.AutoMove;
import com.jaoafa.jdavcspeaker.Framework.Command.CmdRegister;
import com.jaoafa.jdavcspeaker.Framework.Event.EventRegister;
import com.jaoafa.jdavcspeaker.Framework.FunctionHooker;
import com.jaoafa.jdavcspeaker.Lib.*;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.ConfigBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends ListenerAdapter {
    static VisionAPI visionAPI = null;
    static LibTitle libTitle = null;
    static String speakToken = null;
    static String discordToken = null;
    static final String prefix = "/";
    static VCSpeakerArgs args;

    public static void main(String[] _args) {
        args = new VCSpeakerArgs();
        CmdLineParser parser = new CmdLineParser(args);
        try {
            parser.parseArgument(_args);
            if (args.isHelp) {
                parser.printSingleLineUsage(System.out);
                System.out.println();
                System.out.println();
                parser.printUsage(System.out);
                return;
            }
        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            System.out.println();
            parser.printSingleLineUsage(System.out);
            System.out.println();
            System.out.println();
            parser.printUsage(System.out);
            return;
        }

        LibFiles.moveDirFiles();

        LibFlow setupFlow = new LibFlow("Setup");
        setupFlow.header("VCSpeaker Starting");
        setupFlow.action("設定を読み込み中...");

        //Task: Config読み込み

        JSONObject config;
        JSONObject tokenConfig;
        try {
            config = LibJson.readObject(args.configPath);
        } catch (Exception e) {
            setupFlow.error("基本設定の読み込みに失敗しました。");
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        //Task: Config未定義チェック

        boolean missingConfigDetected = false;
        Function<String, String> missingDetectionMsg = "%sが未定義であるため、初期設定に失敗しました。"::formatted;

        //SubTask: Tokenクラスが無かったら終了
        if (!config.has("Token")) {
            setupFlow.error(missingDetectionMsg.apply("Tokenクラス"));
            System.exit(1);
            return;
        } else {
            tokenConfig = config.getJSONObject("Token");
        }

        //SubTask: DiscordTokenの欠落を検知
        if (!config.getJSONObject("Token").has("Discord")) {
            setupFlow.error(missingDetectionMsg.apply("DiscordToken"));
            missingConfigDetected = true;
        }
        //SubTask: SpeakerTokenの欠落を検知
        if (!config.getJSONObject("Token").has("Speaker")) {
            setupFlow.error(missingDetectionMsg.apply("SpeakerToken"));
            missingConfigDetected = true;
        }

        //SubTask: 終了
        if (missingConfigDetected) {
            System.exit(1);
            return;
        }

        if (args.isOnlyRemoveCmd) {
            removeCommands(tokenConfig);
            return;
        }

        if (tokenConfig.has("rollbar")) {
            LibValue.rollbar = Rollbar.init(ConfigBuilder
                .withAccessToken(tokenConfig.getString("rollbar"))
                .environment(getLocalHostName())
                .codeVersion(Main.class.getPackage().getImplementationVersion())
                .handleUncaughtErrors(false)
                .build());

            final Thread.UncaughtExceptionHandler prevHandler =
                Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                e.printStackTrace();
                LibValue.rollbar.critical(e);

                TextChannel channel = LibValue.jda.getTextChannelById(921841152355864586L);
                if (channel != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    String details = sw.toString();
                    InputStream is = new ByteArrayInputStream(details.getBytes(StandardCharsets.UTF_8));
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("JDA-VCSpeaker Error Reporter")
                            .addField("Summary", String.format("%s (%s)", e.getMessage(), e.getClass().getName()), false)
                            .addField("Details", details.substring(0, 1000), false)
                            .addField("Thread Name", t.getName(), false)
                            .setFooter("JDA-VCSpeaker %s".formatted(Main.class.getPackage().getImplementationVersion()))
                            .setColor(Color.RED)
                            .build())
                        .setFiles(FileUpload.fromData(is, "stacktrace.txt"))
                        .queue();
                }

                if (prevHandler != null)
                    prevHandler.uncaughtException(t, e);
            });
            final Consumer<? super Throwable> prevDefaultFailure = RestActionImpl.getDefaultFailure();
            RestActionImpl.setDefaultFailure((e) -> {
                if (e instanceof ErrorResponseException ere) {
                    // 当該ユーザーからブロックされているためにリアクションをつけられない場合、または当該メッセージが存在しない場合は無視
                    if (ere.getErrorResponse() == ErrorResponse.REACTION_BLOCKED || ere.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        return;
                    }
                }
                e.printStackTrace();
                LibValue.rollbar.critical(e);

                TextChannel channel = LibValue.jda.getTextChannelById(921841152355864586L);
                if (channel != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    String details = sw.toString();
                    InputStream is = new ByteArrayInputStream(details.getBytes(StandardCharsets.UTF_8));
                    channel.sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("JDA-VCSpeaker Error Reporter")
                            .addField("Summary", String.format("%s (%s)", e.getMessage(), e.getClass().getName()), false)
                            .addField("Details", details.substring(0, 1000), false)
                            .setFooter("JDA-VCSpeaker %s".formatted(Main.class.getPackage().getImplementationVersion()))
                            .setColor(Color.RED)
                            .build())
                        .setFiles(FileUpload.fromData(is, "stacktrace.txt"))
                        .queue();
                }

                if (prevDefaultFailure != null)
                    prevDefaultFailure.accept(e);
            });
        }

        copyExternalScripts();

        //Task: Token,JDA設定
        speakToken = tokenConfig.getString("Speaker");

        discordToken = tokenConfig.getString("Discord");
        JDABuilder builder = JDABuilder.createDefault(discordToken)
            //JDASettings
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            //Intents
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.MESSAGE_CONTENT
            )
            //EventListeners
            .addEventListeners(new Main())
            .addEventListeners(new FunctionHooker())
            //AutoFunction
            .addEventListeners(new AutoJoin())
            .addEventListeners(new AutoMove())
            .addEventListeners(new AutoDisconnect());

        //自動登録
        new EventRegister(builder);

        //Task: ログイン
        JDA jda;
        try {
            jda = builder.build().awaitReady();
        } catch (InterruptedException e) {
            setupFlow.error("Discordへのログインに失敗しました。");
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        new CmdRegister(jda);

        if (tokenConfig.has("VisionAPI")) {
            try {
                visionAPI = new VisionAPI(tokenConfig.getString("VisionAPI"));
            } catch (Exception e) {
                setupFlow.error("VisionAPIの初期化に失敗しました。関連機能は動作しません。");
                new LibReporter(null, e);
                visionAPI = null;
            }
        }

        try {
            libTitle = new LibTitle();
        } catch (Exception e) {
            setupFlow.error("タイトル設定の読み込みに失敗しました。関連機能は動作しません。");
            new LibReporter(null, e);
            System.exit(1);
            return;
        }

        //Task: 一時ファイル消去
        if (LibFiles.VDirectory.VISION_API_TEMP.exists()) {
            try (Stream<Path> walk = Files.walk(LibFiles.VDirectory.VISION_API_TEMP.getPath(), FileVisitOption.FOLLOW_LINKS)) {
                List<File> missDeletes = walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(f -> !f.delete())
                    .toList();
                if (missDeletes.size() != 0) {
                    new LibFlow("RemoveTempFiles").error(missDeletes.size() + "個のテンポラリファイルの削除に失敗しました。");
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

        //Task: Devのコマンド削除
        try {
            if (tokenConfig.has("VCSDev")) {
                JDA devJda = JDABuilder.createDefault(tokenConfig.getString("VCSDev")).build().awaitReady();
                devJda.getGuilds().forEach(
                    guild -> guild.retrieveCommands().queue(
                        cmds -> cmds.forEach(cmd -> cmd.delete().queue(
                            unused -> new LibFlow("RemoveDevCmd").success("%s から %s コマンドを登録解除しました。", guild.getName(), cmd.getName())
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
            }
        } catch (InterruptedException e) {
            new LibReporter(null, e);
        }
    }

    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    static void removeCommands(JSONObject tokenConfig) {
        LibFlow removeCmdFlow = new LibFlow("RemoveCmd");
        removeCmdFlow.action("登録済みのコマンドをすべて削除します。");
        try {
            JDA jda = JDABuilder
                .createDefault(tokenConfig.getString("Discord"))
                .build()
                .awaitReady();
            List<Command> commands = new ArrayList<>();
            for (Guild guild : jda.getGuilds()) {
                removeCmdFlow.action("サーバ「%s」からコマンド一覧を取得しています…。", guild.getName());
                try {
                    List<Command> guildCommands = guild.retrieveCommands().complete();
                    commands.addAll(guildCommands);
                    removeCmdFlow.success("%d件のコマンドをコマンド削除キューに追加しました。".formatted(guildCommands.size()));
                } catch (ErrorResponseException e) {
                    removeCmdFlow.error("サーバ「%s」からコマンド一覧を取得するのに失敗しました（%s）。", guild.getName(), e.getMessage());
                }
            }


            List<RestAction<Void>> restActions = commands
                .stream()
                .map(Command::delete)
                .collect(Collectors.toList());
            if (restActions.isEmpty()) {
                removeCmdFlow.success("削除するべきコマンドがありませんでした。アプリケーションを終了します。");
                jda.shutdownNow();
                return;
            } else {
                removeCmdFlow.action("%d件のコマンドが見つかりました。削除を開始します。しばらくお待ちください！".formatted(commands.size()));
            }

            RestAction.allOf(restActions).mapToResult().queue(
                s -> {
                    removeCmdFlow.success("すべてのコマンドの削除が完了しました。アプリケーションを終了します。");
                    jda.shutdownNow();
                },
                t -> removeCmdFlow.error("削除中にエラーが発生しました: %s", t.getMessage())
            );
        } catch (InterruptedException e) {
            removeCmdFlow.error("Discordへのログインに失敗しました。");
            new LibReporter(null, e);
        }
    }

    static void copyExternalScripts() {
        LibFiles.VDirectory vDir = LibFiles.VDirectory.EXTERNAL_SCRIPTS;
        String srcDirName = vDir.getPath().toString();
        File destDir = vDir.getPath().toFile();

        final File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        if (!jarFile.isFile()) {
            new LibFlow("Textimg").error("仕様によりexternal_scriptsディレクトリをコピーできません。ビルドしてから実行すると、external_scriptsを使用する機能を利用できます。");
            return;
        }
        try (JarFile jar = new JarFile(jarFile)) {
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(srcDirName + "/") && !entry.isDirectory()) {
                    File dest = new File(destDir, entry.getName().substring(srcDirName.length() + 1));
                    File parent = dest.getParentFile();
                    if (parent != null) {
                        //noinspection ResultOfMethodCallIgnored
                        parent.mkdirs();
                    }
                    new LibFlow("Textimg").success("[external_scripts] Copy " + entry.getName().substring(srcDirName.length() + 1));
                    try (FileOutputStream out = new FileOutputStream(dest); InputStream in = jar.getInputStream(entry)) {
                        byte[] buffer = new byte[8 * 1024];
                        int s;
                        while ((s = in.read(buffer)) > 0) {
                            out.write(buffer, 0, s);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static VisionAPI getVisionAPI() {
        return visionAPI;
    }

    @Nullable
    public static String getDiscordToken() {
        return discordToken;
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
        LibValue.jda = event.getJDA();
        LibAlias.fetchMap();
        LibIgnore.fetchMap();
        new LibFlow("Started").success("VCSPEAKER!!!!!!!!!!!!!!!!!!!!STARTED!!!!!!!!!!!!:tada::tada:");
    }

    @NotNull
    public static OptionMapping getExistsOption(SlashCommandInteractionEvent event, String name) {
        OptionMapping option = event.getOption(name);
        if (option == null) {
            throw new IllegalArgumentException();
        }
        return option;
    }

    public static VCSpeakerArgs getArgs() {
        return args;
    }
}
