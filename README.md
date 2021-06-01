![image](https://user-images.githubusercontent.com/53546237/120327499-cbc73080-c324-11eb-9298-85a45789f0ee.png)

JDA + Lavaplayer で作られている VCSpeaker のリポジトリです。

## Features

- チャット読み上げチャンネルに投稿されたチャットを読み上げます。
- 誰かが VC に参加・移動・退出した際、チャット読み上げチャンネルで通知し読み上げます。
- VCSpeaker が VC に参加していない際に誰かが VC に参加した場合、VCSpeaker も参加します。
- VC からユーザーが全員退出した場合、VCSpeaker も退出します。

## Commands

デフォルトプレフィックスは `;` です。コマンドの最初にプレフィックスを付けてください(例: `;alias`)  
`...` とある引数は、以降の引数も受け付けます (`ignore add contain a b c` -> `a b c` を含む場合に読み上げなくする)

### `alias`: 読み上げ文字列置き換え

任意の文字列を別の文字列に置き換えて読み上げる機能です。

- `alias add <from> <to>`: `<from>` を `<to>` に置き換えるように設定します。
- `alias remove <from>`: `<from>` の置き換えを削除します。
  - (同等引数: `alias <rm|delete|del> <from>`)
- `alias list`: 置き換え(エイリアス)一覧を取得します。

### `clear`: 読み上げキュー削除

読み上げのキューをクリアします。

### `default`: デフォルトパラメーター

ユーザー・サーバ毎に読み上げパラメーターのデフォルトを設定します。

- `default user <params...>`: あなたのデフォルトパラメーターを設定します。`<params...>` には `speaker:show speed:200` といったメッセージで指定するパラメーターと同じように入力して下さい
  - (同等引数: `default <params...>`)
- `default user get`: 現在設定しているあなたのデフォルトパラメーターを表示します。
  - (同等引数: `default get`)
- `default user reset`: あなたのデフォルトパラメーターをリセットします。
  - (同等引数: `default reset`)

### `disconnect`: 切断

VCSpeaker を VC から切断します。

### `ignore`: 読み上げ無視

任意の文字列が含む・一致する場合に読み上げを無視する機能です。

- `ignore add contain <text...>`: `<text...>` を含む場合に読み上げないように設定します。
  - (同等引数: `ignore add contains <text...>`)
- `ignore add equal <text...>`: `<text...>` と一致する場合に読み上げないように設定します。
  - (同等引数: `ignore add equals <text...>`)
- `ignore remove contain <text...>`: `<text...>` を含んでも読み上げるように設定を解除します。
  - (同等引数: `ignore <rm|delete|del> contains <text...>`)
- `ignore add equal <text...>`: `<text...>` と一致しても読み上げるように設定を解除します。
  - (同等引数: `ignore <rm|delete|del> equals <text...>`)
- `ignore list`: 読み上げを無視する設定情報を表示します。

### `restart`: 再起動

VCSpeaker を再起動します。

### `summon`: 召喚

VCSpeaker を現在入っている VC に参加させます。

### `vcspeaker`: VCSpeaker 関連

VCSpeaker の初期設定など、VCSpeaker 関連の設定を行います。サーバの管理者権限が必要です。

- `vcspeaker server add`: サーバを VCSpeaker の読み上げサーバとして設定します。コマンドを実行したチャンネルをチャット読み上げチャンネルとして設定します。
- `vcspeaker server remove`: サーバを VCSpeaker の読み上げサーバから削除します。
- `vcspeaker server notifychannel <channel>`: VCSpeaker の会話開始通知チャンネルを設定します。

## Parameters

### speaker

- show
- haruka
- hikari (デフォルト)
- takeru
- santa 
- bear

### emotion

※ haruka, hikari, takeru, santa, bearのみ

- happiness
- anger
- sadness

### emotion_level

1 ～ 4

### pitch

50 ～ 200 (%) デフォルト100

### speed

50 ～ 400 (%) デフォルト120
