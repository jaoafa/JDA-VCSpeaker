package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Player.AudioPlayerSendHandler;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import okhttp3.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
                AudioTrackInfo trackInfo = new AudioTrackInfo("SpeakText","VCSpeaker",10000,"VCSpeaker",true,"VCSpeaker");
                AudioTrack audioTrack = audioPlayerManager.decodeTrackDetails(trackInfo,contentBuilder.toString().getBytes(StandardCharsets.UTF_8));

                audioTrack.setUserData("vcspeaker");
                AudioPlayerManager manager = new DefaultAudioPlayerManager();
                //AudioSourceManagers.registerLocalSource(manager);
                AudioPlayer messagePlayer = manager.createPlayer();
                AudioPlayerSendHandler sendHandler = new AudioPlayerSendHandler(messagePlayer);
                AudioManager audioManager = guild.getAudioManager();
                audioManager.setSendingHandler(sendHandler);
                //再生
                //AudioPlayerManager manager = new DefaultAudioPlayerManager();
                //manager.registerSourceManager(new LocalAudioSourceManager());
                //AudioSourceManagers.registerRemoteSources(manager);
                //manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
                //AudioManager audioManager = guild.getAudioManager();
                //VoiceChannel connectedChannel = member.getVoiceState().getChannel();
                //TextToVoiceUtil.voice(connectedChannel,audioManager);
                pm.getGuildMusicManager(guild).scheduler.queue(audioTrack);//NullPointer。 再生されない。

                pm.getGuildMusicManager(guild).player.playTrack(audioTrack);//何もなし。再生されない。

                pm.getGuildMusicManager(guild).player.startTrack(audioTrack,false);

                messagePlayer.startTrack(audioTrack,true);

                messagePlayer.startTrack(audioTrack,false);

                messagePlayer.playTrack(audioTrack);


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
    public byte[] convertFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        return IOUtils.toByteArray(inputStream);
    }
}

