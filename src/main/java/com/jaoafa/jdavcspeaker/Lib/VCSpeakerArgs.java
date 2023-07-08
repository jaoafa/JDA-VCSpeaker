package com.jaoafa.jdavcspeaker.Lib;

import org.kohsuke.args4j.Option;

import java.io.File;

public class VCSpeakerArgs {
    @Option(name = "--help",
            aliases = "-h",
            usage = "ヘルプを表示",
            help = true)
    public boolean isHelp;

    @SuppressWarnings("CanBeFinal")
    @Option(name = "--config-file",
            aliases = "-f",
            metaVar = "FILEPATH",
            usage = "設定ファイルのパス")
    public File configPath = LibFiles.VFile.CONFIG.getPath().toFile();

    @Option(name = "--only-remove-cmd",
            usage = "すべてのスラッシュコマンドの登録を解除")
    public boolean isOnlyRemoveCmd;

    @Option(name = "--disable-auto-join",
            usage = "ユーザのVC参加によるVCへの自動参加を無効化")
    public boolean isDisableAutoJoin;

    @Option(name = "--disable-auto-join-by-message",
            usage = "メッセージ送信によるVCへの自動参加を無効化")
    public boolean isDisableAutoJoinByMessage;

    @Option(name = "--disable-auto-move",
            usage = "VC間の自動移動を無効化")
    public boolean isDisableAutoMove;

    @Option(name = "--disable-auto-disconnect",
            usage = "VCからの自動退出を無効化")
    public boolean isDisableAutoDisconnect;

    @Option(name = "--disable-user-activity-notify",
            usage = "ユーザーアクティビティ（参加・退出・移動）の通知を無効化")
    public boolean isDisableUserActivityNotify;

    @Option(name = "--disable-golive-notify",
            usage = "GoLive開始・終了時の通知を無効化")
    public boolean isDisableGoLiveNotify;

    @SuppressWarnings("CanBeFinal")
    @Option(name = "--format-message",
            usage = """
                テキストメッセージ読み上げのフォーマット
                {username}: 投稿者ユーザー名
                {nickname}: 投稿者ニックネーム（未指定の場合ユーザー名）
                {message}: メッセージテキスト
                """,
            metaVar = "FORMAT")
    public String formatMessage = "{message}";
}
