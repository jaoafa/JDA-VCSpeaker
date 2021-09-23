package com.jaoafa.jdavcspeaker.Lib;

import org.kohsuke.args4j.Option;

import java.io.File;

public class VCSpeakerArgs {
    @Option(name = "--help",
            aliases = "-h",
            usage = "ヘルプを表示",
            help = true)
    public boolean isHelp;

    @Option(name = "--config-file",
            aliases = "-f",
            metaVar = "FILEPATH",
            usage = "設定ファイルのパス")
    public File configPath = new File("VCSpeaker.json");

    @Option(name = "--only-remove-cmd",
            usage = "すべてのスラッシュコマンドの登録を解除")
    public boolean isOnlyRemoveCmd;

    @Option(name = "--disable-auto-join",
            usage = "VCへの自動参加を無効化")
    public boolean isDisableAutoJoin;

    @Option(name = "--disable-auto-disconnect",
            usage = "VCからの自動退出を無効化")
    public boolean isDisableAutoDisconnect;

    @Option(name = "--disable-user-activity-notify",
            usage = "ユーザーアクティビティ（参加・退出・移動）の通知を無効化")
    public boolean isDisableUserActivityNotify;

    @Option(name = "--disable-golive-notify",
            usage = "GoLive開始・終了時の通知を無効化")
    public boolean isDisableGoLiveNotify;
}
