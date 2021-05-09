package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VoiceText {
    public static void speak(Message message, String speakText) {
        if(speakText.length() == 0){
            return;
        }
        try {
            try {
                OkHttpClient client = new OkHttpClient();
                ParamCheck.toForm createForm = new ParamCheck.toForm(speakText, message.getTextChannel());
                String hexString = DigestUtils.md5Hex(createForm.formatText);
                FormBody.Builder form = createForm.form;
                Request request = new Request.Builder()
                        .post(form.build())
                        .url("https://api.voicetext.jp/v1/tts")
                        .header("Authorization", Credentials.basic(LibJson.readObject("./VCSpeaker.json").getString("SpeakToken"), ""))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    if (body == null) {
                        System.out.println("Warning: response.body() is null.");
                        return;
                    }
                    if (!response.isSuccessful()) {
                        System.out.println("Error: " + response.code());
                        System.out.println(body.string());
                        return;
                    }
                    System.setProperty("file.encoding", "UTF-8");
                    Files.write(Paths.get("./Temp/" + hexString + ".mp3"), body.bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                TrackInfo info = new TrackInfo(message);
                PlayerManager.getINSTANCE().loadAndPlay(info, "./Temp/" + hexString + ".mp3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
