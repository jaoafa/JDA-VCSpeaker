package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VoiceText {
    public static void speak(TextChannel channel, String text, String userdata) {
        try {
            try {
                File newdir = new File("./Temp");
                newdir.mkdir();
                
                OkHttpClient client = new OkHttpClient();
                ParamCheck.toForm createForm = new ParamCheck.toForm(text, channel);
                String hexString = DigestUtils.md5Hex(createForm.formatText);
                FormBody.Builder form = createForm.form;
                Request request = new Request.Builder()
                        .post(form.build())
                        .url("https://api.voicetext.jp/v1/tts")
                        .header("Authorization", Credentials.basic(LibJson.read("./VCSpeaker.json").getString("SpeakToken"), ""))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.out.println("Error: " + response.code());
                        System.out.println(response.body().string());
                        return;
                    }
                    System.setProperty("file.encoding", "UTF-8");
                    Files.write(Paths.get("./Temp/"+hexString+".wav"), response.body().bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder contentBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader("./Temp/"+hexString+".wav"))) {
                    String sCurrentLine;
                    while ((sCurrentLine = br.readLine()) != null) {
                        contentBuilder.append(sCurrentLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlayerManager.getINSTANCE().loadAndPlay(channel, "./Temp/"+hexString+".wav", userdata);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
