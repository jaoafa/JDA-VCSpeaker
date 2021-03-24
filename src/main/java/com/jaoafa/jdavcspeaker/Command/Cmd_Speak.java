package com.jaoafa.jdavcspeaker.Command;

import com.jaoafa.jdavcspeaker.CmdInterface;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Util.JSONUtil;
import com.jaoafa.jdavcspeaker.Util.VoiceText;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Cmd_Speak implements CmdInterface {
    @Override
    public void onCommand(JDA jda, Guild guild, MessageChannel channel, Member member, Message message, String[] args) {
        VoiceText.speak((TextChannel) channel,message.getContentRaw().replace(";speak ",""),null);
        /*try {
            try {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder form = new FormBody.Builder();
                form.add("text", message.getContentRaw().substring(7));
                form.add("speaker", "show");
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
                    System.setProperty("file.encoding", "UTF-8");
                    Files.write(Paths.get("./speak.wav"), response.body().bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder contentBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader("./speak.wav"))) {
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine()) != null) {
                        contentBuilder.append(sCurrentLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlayerManager.getINSTANCE().loadAndPlay((TextChannel) message.getChannel(), "./speak.wav");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}

