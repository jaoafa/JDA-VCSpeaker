package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Player.AudioPlayerSendHandler;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.JSONException;

import okhttp3.*;
import ws.schild.jave.*;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Cmd_Speak implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        try {
            try {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder form = new FormBody.Builder();
                form.add("text", message.getContentRaw());
                form.add("speaker","show");
                Request request = new Request.Builder()
                        .post(form.build())
                        .url("https://api.voicetext.jp/v1/tts")
                        .header("Authorization", Credentials.basic(JSONUtil.read("./VCSpeaker.json").getString("SpeakToken"), ""))
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
                //AudioTrackInfo trackInfo = new AudioTrackInfo("SpeakText","VCSpeaker",10000,"VCSpeaker",true,"VCSpeaker");
                //AudioTrack audioTrack = audioPlayerManager.decodeTrackDetails(trackInfo,contentBuilder.toString().getBytes(StandardCharsets.UTF_8));

                //convertFile(new File("./speak.wav"));

                /*AudioPlayerManager manager = new DefaultAudioPlayerManager();
                AudioSourceManagers.registerLocalSource(manager);
                AudioPlayer messagePlayer = manager.createPlayer();
                AudioPlayerSendHandler sendHandler = new AudioPlayerSendHandler(messagePlayer);
                AudioManager audioManager = guild.getAudioManager();
                audioManager.setSendingHandler(sendHandler);
                AudioSourceManagers.registerRemoteSources(manager);
                AudioSourceManagers.registerLocalSource(manager);*/
                //PlayerManager.getINSTANCE().loadAndPlay(message.getTextChannel(),"./speak.mp3");
                PlayerManager.getINSTANCE().loadAndPlay((TextChannel) message.getChannel(),"./speak.wav");
                /*manager.loadItemOrdered(PlayerManager.getINSTANCE().getGuildMusicManager(guild),"https://www.youtube.com/watch?v=MOBYK_reo-4", new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        System.out.println("Track Loaded on Speak.java");
                        messagePlayer.playTrack(audioTrack);

                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {

                    }

                    @Override
                    public void noMatches() {

                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        System.out.println("loadfailed on Speak.java");
                        e.printStackTrace();
                    }
                });*/
                //manager.loadItem("./speak.wav",);
                //再生
                //AudioPlayerManager manager = new DefaultAudioPlayerManager();
                //manager.registerSourceManager(new LocalAudioSourceManager());
                //AudioSourceManagers.registerRemoteSources(manager);
                //manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
                //AudioManager audioManager = guild.getAudioManager();
                //VoiceChannel connectedChannel = member.getVoiceState().getChannel();
                //TextToVoiceUtil.voice(connectedChannel,audioManager);
                /*pm.getGuildMusicManager(guild).scheduler.queue(audioTrack);//NullPointer。 再生されない。

                pm.getGuildMusicManager(guild).player.playTrack(audioTrack);//何もなし。再生されない。

                pm.getGuildMusicManager(guild).player.startTrack(audioTrack,false);

                messagePlayer.startTrack(audioTrack,true);

                messagePlayer.startTrack(audioTrack,false);

                messagePlayer.playTrack(audioTrack);*/


            } catch (JSONException e) {
                e.printStackTrace();
            }



            //プレイヤーマネージャー宣言
            
            /*AudioPlayerManager manager = new DefaultAudioPlayerManager();
            AudioSourceManagers.registerRemoteSources(manager);
            manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);*/
            
            //喋ったデータをAudioInputStream型で取得


            /*SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(stream.getFormat());
            line.start();
            byte[] buffer = new byte[COMMON_PCM_S16_BE.maximumChunkSize()];
            int chunkSize;
            while ((chunkSize = stream.read(buffer)) >= 0) {
                line.write(buffer, 0, chunkSize);
            }*/
            
            /*DefaultAudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

            AudioInputStream stream = context.getResponse(message.getContentRaw()).audioInputStream();
            //エクスポート用ファイル
            File file = new File("./speak.wav");
            //ここで変換してエクスポート
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);*/

            /*byte[] buffer = new byte[Integer.parseInt(String.valueOf(stream.getFrameLength()))];
            int readedbuffer = stream.read(buffer);
            PrefixUtil.simple.add(String.valueOf(readedbuffer));
            FileWriter filewriter = new FileWriter(file);
            for (int count = 0; buffer.length>count; count++){
                System.out.println(buffer[count]);
                filewriter.write(buffer[count]+" ");
            }
            filewriter.close();*/


            /*AudioSourceManagers.registerRemoteSources(manager);
            manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);

            AudioPlayer player = manager.createPlayer();
            AudioInputStream stream = context.getResponse(args[0]).audioInputStream();
            audioP.startTrack(stream,false);
            SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            line.open(stream.getFormat());
            line.start();

            byte[] buffer = new byte[COMMON_PCM_S16_BE.maximumChunkSize()];
            int chunkSize;

            while ((chunkSize = stream.read(buffer)) >= 0) {
                line.write(buffer, 0, chunkSize);
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void convertFile(File file) throws IOException {
        try {
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp3");
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("aac");
            audio.setBitRate(256000);
            audio.setChannels(1);
            audio.setSamplingRate(88200);
            attrs.setAudioAttributes(audio);
            File source = new File("./speak.wav");
            File dest = new File("./speak.mp3");
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), dest, attrs);
            if (!dest.exists() || dest.length() == 0) {
                System.out.println("encode failer.");
            }
        } catch (EncoderException e) {
            System.out.println("Occured EncoderException.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Occured Exception.");
        }
    }
}

