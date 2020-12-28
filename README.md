# JDA-VCSpeaker
JDA+Lavaplayerで作られる(はず)のVCSpeakerのリポジトリです。

## 進捗
- ✅Exception無しで最後まで動く
- ✅wavファイルは正常にダウンロード可能
- 🚧AudioTrackにデコード可能(<-正常に出来てるか怪しい...?)
- 🛑Discordでの再生は不可

```java
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder form = new FormBody.Builder();
                form.add("text", message.getContentRaw());
                form.add("speaker","show");
                Request request = new Request.Builder()
                        .post(form.build())
                        .url("https://api.voicetext.jp/v1/tts")
                        .header("Authorization", Credentials.basic("ここにToken", ""))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.out.println("Error: " + response.code());
                        System.out.println(response.body().string());
                        return;
                    }
                    System.out.println("Successful");
                    System.setProperty("file.encoding","UTF-8");
                    Files.write(Paths.get("./speak.wav"), response.body().bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //AudioTrackにデコード
                //プレイヤーマネージャー宣言
                PlayerManager pm = PlayerManager.getINSTANCE();
                DefaultAudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
                StringBuilder contentBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader("./speak.wav"))) {
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine()) != null) {
                        contentBuilder.append(sCurrentLine);
                        //.append("\n")
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AudioTrackInfo trackInfo = new AudioTrackInfo("SpeakText","VCSpeaker",new File("./speak.wav").length(),"VCSpeaker",false,"VCSpeaker");
                AudioTrack audioTrack = audioPlayerManager.decodeTrackDetails(trackInfo,contentBuilder.toString().getBytes(StandardCharsets.UTF_8));
                pm.getGuildMusicManager(guild).player.playTrack(audioTrack);
                //以下コメントアウト
                //AudioPlayerManager manager = new DefaultAudioPlayerManager();
                //manager.registerSourceManager(new LocalAudioSourceManager());
                //AudioSourceManagers.registerRemoteSources(manager);
                //manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
                /*AudioManager audioManager = guild.getAudioManager();
                VoiceChannel connectedChannel = member.getVoiceState().getChannel();
                TextToVoiceUtil.voice(connectedChannel,audioManager);*/
