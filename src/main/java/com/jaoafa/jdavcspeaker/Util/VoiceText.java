package com.jaoafa.jdavcspeaker.Util;

import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.*;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class VoiceText {
    public static void speak(TextChannel channel,String text){
        final String[] speaktext = {text};
        final String[] speaker = new String[1];
        final int[] speed = new int[1];
        final boolean[] setEmotion = {false};
        final String[] emotion = new String[1];
        final int[] emotion_lv= new int[1];
        final int pitch[] = new int[1];

        //パラメーターチェック
        Arrays.stream(text.split(" ")).forEach(s -> {
            //speakerCheck
            if (s.startsWith("speaker:")){
                String speakerText = s.replace("speaker:","");
                //wrongSpeakerCheck
                if (speakerText.equals("show")||speakerText.equals("haruka")||
                        speakerText.equals("hikari")||speakerText.equals("takeru")||
                        speakerText.equals("santa")||speakerText.equals("bear")) {
                    speaker[0] = speakerText;
                    speaktext[0] = speaktext[0].replace(s,"");
                }else{
                    EmbedBuilder speakerWrong = new EmbedBuilder();
                    speakerWrong.setTitle(":x: Wrong Speaker!");
                    speakerWrong.setDescription("`speaker:"+speakerText+"`は存在しません。");
                    speakerWrong.setColor(EmbedColors.error);
                    channel.sendMessage(speakerWrong.build()).queue();
                    return;
                }
            }

            //speedCheck
            if (s.startsWith("speed:")){
                int speedInt;
                try {
                    speedInt = Integer.parseInt(s.replace("speed:",""));
                }catch (NumberFormatException e){
                    EmbedBuilder numberFormatWrong = new EmbedBuilder();
                    numberFormatWrong.setTitle(":x: Wrong Speed!");
                    numberFormatWrong.setDescription("`speed:`は半角数字で指定してください。");
                    numberFormatWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberFormatWrong.build()).queue();
                    return;
                }
                if (50<=speedInt&&speedInt<=400){
                    speed[0] = speedInt;
                    speaktext[0] = speaktext[0].replace(s,"");
                }else {
                    EmbedBuilder numberWrong = new EmbedBuilder();
                    numberWrong.setTitle(":x: Wrong Speed!");
                    numberWrong.setDescription("`speed:`は50以上400以下で指定してください。");
                    numberWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberWrong.build()).queue();
                    return;
                }
            }

            //emotionCheck
            if (s.startsWith("emotion:")){
                String emotionText = s.replace("emotion:","");
                //wrongEmotionCheck
                if (emotionText.equals("happy")||emotionText.equals("sad")||
                        emotionText.equals("anger")) {
                    emotionText = emotionText.replace("happy","happiness");
                    emotionText = emotionText.replace("sad","sadness");
                    setEmotion[0] = true;
                    emotion[0] = emotionText;
                    speaktext[0] = speaktext[0].replace(s,"");
                }else{
                    EmbedBuilder emotionWrong = new EmbedBuilder();
                    emotionWrong.setTitle(":x: Wrong Emotion!");
                    emotionWrong.setDescription("`emotion:"+emotionText+"`は存在しません。");
                    emotionWrong.setColor(EmbedColors.error);
                    channel.sendMessage(emotionWrong.build()).queue();
                    return;
                }
            }

            //pitchCheck
            if (s.startsWith("pitch:")){
                int pitchInt;
                try {
                    pitchInt = Integer.parseInt(s.replace("pitch:",""));
                }catch (NumberFormatException e){
                    EmbedBuilder numberFormatWrong = new EmbedBuilder();
                    numberFormatWrong.setTitle(":x: Wrong Pitch!");
                    numberFormatWrong.setDescription("`pitch:`は半角数字で指定してください。");
                    numberFormatWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberFormatWrong.build()).queue();
                    return;
                }
                if (50<=pitchInt&&pitchInt<=200){
                    pitch[0] = pitchInt;
                    speaktext[0] = speaktext[0].replace(s,"");
                }else {
                    EmbedBuilder numberWrong = new EmbedBuilder();
                    numberWrong.setTitle(":x: Wrong Pitch!");
                    numberWrong.setDescription("`pitch:`は50以上200以下で指定してください。");
                    numberWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberWrong.build()).queue();
                    return;
                }
            }

            //emoLvCheck
            if (s.startsWith("emotion_level:")){
                int emotionLvInt;
                try {
                    emotionLvInt = Integer.parseInt(s.replace("emotion_level:",""));
                }catch (NumberFormatException e){
                    EmbedBuilder numberFormatWrong = new EmbedBuilder();
                    numberFormatWrong.setTitle(":x: Wrong Emotion Level!");
                    numberFormatWrong.setDescription("`emotion_level:`は半角数字で指定してください。");
                    numberFormatWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberFormatWrong.build()).queue();
                    return;
                }
                if (1<=emotionLvInt&&emotionLvInt<=4){
                    emotion_lv[0] = emotionLvInt;
                    speaktext[0] = speaktext[0].replace(s,"");
                }else {
                    EmbedBuilder numberWrong = new EmbedBuilder();
                    numberWrong.setTitle(":x: Wrong Emotion Level!");
                    numberWrong.setDescription("`emotion_level:`は1以上4以下で指定してください。");
                    numberWrong.setColor(EmbedColors.error);
                    channel.sendMessage(numberWrong.build()).queue();
                    return;
                }
            }
        });

        //defaultValue
        if (speaker[0] == null){
            speaker[0] = "hikari";
        }
        if (speed[0] == 0){
            speed[0] = 100;
        }
        if (pitch[0] == 0){
            pitch[0] = 100;
        }
        if (emotion_lv[0] == 0){
            emotion_lv[0] = 2;
        }


        try {
            try {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder form = new FormBody.Builder();
                form.add("text", EmojiParser.parseToAliases(speaktext[0]).replace(":",""));
                System.out.println(EmojiParser.parseToAliases(speaktext[0]).replace(":",""));
                form.add("speaker", speaker[0]);
                form.add("speed", String.valueOf(speed[0]));
                form.add("pitch", String.valueOf(pitch[0]));
                if (setEmotion[0]){
                    form.add("emotion", emotion[0]);
                    form.add("emotion_level", String.valueOf(emotion_lv[0]));
                }

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
                PlayerManager.getINSTANCE().loadAndPlay(channel, "./speak.wav");
                /*channel.sendMessage("**Debug:パラメーター**").queue();
                channel.sendMessage(speaker[0]+":speaker").queue();
                channel.sendMessage(speed[0]+":speed").queue();
                channel.sendMessage(pitch[0]+":pitch").queue();
                channel.sendMessage(emotion[0]+":emotion").queue();
                channel.sendMessage(emotion_lv[0]+":emotionlv").queue();
                channel.sendMessage(setEmotion[0]+":emotionEnable").queue();*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
