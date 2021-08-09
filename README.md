    ╔═════════════════════════════════════════════════════╗
    ║  _    _____________                  __             ║▓
    ║ | |  / / ____/ ___/____  ___  ____ _/ /_____  _____ ║▓
    ║ | | / / /    \__ \/ __ \/ _ \/ __ `/ //_/ _ \/ ___/ ║▓
    ║ | |/ / /___ ___/ / /_/ /  __/ /_/ / ,< /  __/ /     ║▓
    ║ |___/\____//____/ .___/\___/\__,_/_/|_|\___/_/      ║▓
    ║                /_/                                  ║▓
    ║                                                     ║▓
    ╚═════════════════════════════════════════════════════╝▓
     ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓

JDA + Lavaplayer で作られている VCSpeaker のリポジトリです。

## Features

- チャット読み上げチャンネルに投稿されたチャットを読み上げます。
- 誰かが VC に参加・移動・退出した際、チャット読み上げチャンネルで通知し読み上げます。
- VCSpeaker が VC に参加していない際に誰かが VC に参加した場合、VCSpeaker も参加します。
- VC からユーザーが全員退出した場合、VCSpeaker も退出します。

## Commands

すべてのコマンドは [スラッシュコマンド (Slash Commands)](https://discord.com/developers/docs/interactions/slash-commands) にて実装されています。  
スラッシュ `/` を入力すると、以下のようにサジェストされます。`VCSpeaker` ユーザーが定義しているスラッシュコマンドを使用していることを確認してください。  
引数は正しく指定する必要があります。

![20210810-083647-Discord-BDFMFiWD0j](https://user-images.githubusercontent.com/8929706/128787386-04b95995-62a4-48ab-b60a-e8e78833ebd3.png)

### `alias`: 読み上げ文字列置き換え

任意の文字列を別の文字列に置き換えて読み上げる機能です。

- `alias add from:<from> to:<to>`: `<from>` を `<to>` に置き換えるように設定します。
- `alias remove from:<from>`: `<from>` の置き換えを削除します。
- `alias list`: 置き換え(エイリアス)一覧を取得します。

### `clear`: 読み上げキュー削除

読み上げのキューをクリアします。

### `default`: デフォルトパラメーター

ユーザー・サーバ毎に読み上げパラメーターのデフォルトを設定します。

- `default set params:<params...>`: あなたのデフォルトパラメーターを設定します。`<params...>` には `speaker:show speed:200` といったメッセージで指定するパラメーターと同じように入力して下さい
- `default get`: 現在設定しているあなたのデフォルトパラメーターを表示します。
- `default reset`: あなたのデフォルトパラメーターをリセットします。

### `disconnect`: 切断

VCSpeaker を VC から切断します。

### `ignore`: 読み上げ無視

任意の文字列が含む・一致する場合に読み上げを無視する機能です。

- `ignore add contain text:<text...>`: `<text...>` を含む場合に読み上げないように設定します。
- `ignore add equal text:<text...>`: `<text...>` と一致する場合に読み上げないように設定します。
- `ignore remove contain text:<text...>`: `<text...>` を含んでも読み上げるように設定を解除します
- `ignore add equal text:<text...>`: `<text...>` と一致しても読み上げるように設定を解除します。
- `ignore list`: 読み上げを無視する設定情報を表示します。

### `restart`: 再起動

VCSpeaker を再起動します。

### `skip`: 読み上げ中のテキストをスキップ

現在読み上げているテキストの読み上げをスキップします。上手く動かない場合は `clear` をお試しください。

### `summon`: 召喚

VCSpeaker を現在入っている VC に参加させます。

### `title`: VC のチャンネル名一時的変更

参加している VC のチャンネル名を一時的に変更します。変更後、参加者全員が退出した時点で元のタイトルに戻ります。

- `title title:<title>`: `<title>` に VC のチャンネル名を変更します。

### `vcspeaker`: VCSpeaker 関連

VCSpeaker の初期設定など、VCSpeaker 関連の設定を行います。サーバの管理者権限が必要です。

- `vcspeaker server add channel:<channel>`: サーバを VCSpeaker の読み上げサーバとして設定します。`<channel>` にはチャット読み上げチャンネルを指定します。
- `vcspeaker server remove`: サーバを VCSpeaker の読み上げサーバから削除します。
- `vcspeaker server notify channel:<channel>`: VCSpeaker の会話開始通知チャンネルを設定します。

### `vcname`: VC 名の保存

現在の VC 名をデフォルト設定として保存します。サーバの管理者権限が必要です。

- `vcname save`: 現在参加している VC のチャンネル名を保存します。
- `vcname saveall`: 全 VC のチャンネル名を保存します。

## Parameters

### speaker

- show
- haruka
- hikari (デフォルト)
- takeru
- santa
- bear

### emotion

※ haruka, hikari, takeru, santa, bear のみ

- happiness
- anger
- sadness

### emotion_level

1 ～ 4

### pitch

50 ～ 200 (%) デフォルト 100

### speed

50 ～ 400 (%) デフォルト 120
