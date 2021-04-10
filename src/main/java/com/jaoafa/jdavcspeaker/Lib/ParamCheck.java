package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.FormBody;

import java.util.Arrays;

public class ParamCheck {
    final public static class toForm {
        public String formatText;
        public FormBody.Builder form;

        public toForm(String text, TextChannel channel) {
            final String[] speaktext = {text};
            final String[] speaker = new String[1];
            final int[] speed = new int[1];
            final boolean[] setEmotion = {false};
            final String[] emotion = new String[1];
            final int[] emotion_lv = new int[1];
            final int[] pitch = new int[1];

            //パラメーターチェック
            Arrays.stream(text.split(" ")).forEach(s -> {
                //speakerCheck
                if (s.startsWith("speaker:")) {
                    String speakerText = s.replace("speaker:", "");
                    //wrongSpeakerCheck
                    if (speakerText.equals("show") || speakerText.equals("haruka") ||
                            speakerText.equals("hikari") || speakerText.equals("takeru") ||
                            speakerText.equals("santa") || speakerText.equals("bear")) {
                        speaker[0] = speakerText;
                        speaktext[0] = speaktext[0].replace(s, "");
                    } else {
                        EmbedBuilder speakerWrong = new EmbedBuilder();
                        speakerWrong.setTitle(":x: Wrong Speaker!");
                        speakerWrong.setDescription("`speaker:" + speakerText + "`は存在しません。");
                        speakerWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(speakerWrong.build()).queue();
                        return;
                    }
                }

                //speedCheck
                if (s.startsWith("speed:")) {
                    int speedInt;
                    try {
                        speedInt = Integer.parseInt(s.replace("speed:", ""));
                    } catch (NumberFormatException e) {
                        EmbedBuilder numberFormatWrong = new EmbedBuilder();
                        numberFormatWrong.setTitle(":x: Wrong Speed!");
                        numberFormatWrong.setDescription("`speed:`は半角数字で指定してください。");
                        numberFormatWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberFormatWrong.build()).queue();
                        return;
                    }
                    if (50 <= speedInt && speedInt <= 400) {
                        speed[0] = speedInt;
                        speaktext[0] = speaktext[0].replace(s, "");
                    } else {
                        EmbedBuilder numberWrong = new EmbedBuilder();
                        numberWrong.setTitle(":x: Wrong Speed!");
                        numberWrong.setDescription("`speed:`は50以上400以下で指定してください。");
                        numberWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberWrong.build()).queue();
                        return;
                    }
                }

                //emotionCheck
                if (s.startsWith("emotion:")) {
                    String emotionText = s.replace("emotion:", "");
                    //wrongEmotionCheck
                    if (emotionText.equals("happy") || emotionText.equals("sad") ||
                            emotionText.equals("anger")) {
                        emotionText = emotionText.replace("happy", "happiness");
                        emotionText = emotionText.replace("sad", "sadness");
                        setEmotion[0] = true;
                        emotion[0] = emotionText;
                        speaktext[0] = speaktext[0].replace(s, "");
                    } else {
                        EmbedBuilder emotionWrong = new EmbedBuilder();
                        emotionWrong.setTitle(":x: Wrong Emotion!");
                        emotionWrong.setDescription("`emotion:" + emotionText + "`は存在しません。");
                        emotionWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(emotionWrong.build()).queue();
                        return;
                    }
                }

                //pitchCheck
                if (s.startsWith("pitch:")) {
                    int pitchInt;
                    try {
                        pitchInt = Integer.parseInt(s.replace("pitch:", ""));
                    } catch (NumberFormatException e) {
                        EmbedBuilder numberFormatWrong = new EmbedBuilder();
                        numberFormatWrong.setTitle(":x: Wrong Pitch!");
                        numberFormatWrong.setDescription("`pitch:`は半角数字で指定してください。");
                        numberFormatWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberFormatWrong.build()).queue();
                        return;
                    }
                    if (50 <= pitchInt && pitchInt <= 200) {
                        pitch[0] = pitchInt;
                        speaktext[0] = speaktext[0].replace(s, "");
                    } else {
                        EmbedBuilder numberWrong = new EmbedBuilder();
                        numberWrong.setTitle(":x: Wrong Pitch!");
                        numberWrong.setDescription("`pitch:`は50以上200以下で指定してください。");
                        numberWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberWrong.build()).queue();
                        return;
                    }
                }

                //emoLvCheck
                if (s.startsWith("emotion_level:")) {
                    int emotionLvInt;
                    try {
                        emotionLvInt = Integer.parseInt(s.replace("emotion_level:", ""));
                    } catch (NumberFormatException e) {
                        EmbedBuilder numberFormatWrong = new EmbedBuilder();
                        numberFormatWrong.setTitle(":x: Wrong Emotion Level!");
                        numberFormatWrong.setDescription("`emotion_level:`は半角数字で指定してください。");
                        numberFormatWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberFormatWrong.build()).queue();
                        return;
                    }
                    if (1 <= emotionLvInt && emotionLvInt <= 4) {
                        emotion_lv[0] = emotionLvInt;
                        speaktext[0] = speaktext[0].replace(s, "");
                    } else {
                        EmbedBuilder numberWrong = new EmbedBuilder();
                        numberWrong.setTitle(":x: Wrong Emotion Level!");
                        numberWrong.setDescription("`emotion_level:`は1以上4以下で指定してください。");
                        numberWrong.setColor(LibEmbedColor.error);
                        channel.sendMessage(numberWrong.build()).queue();
                        return;
                    }
                }
            });

            if (speaker[0] == null) {
                speaker[0] = "hikari";
            }
            if (speed[0] == 0) {
                speed[0] = 100;
            }
            if (pitch[0] == 0) {
                pitch[0] = 100;
            }
            if (emotion_lv[0] == 0) {
                emotion_lv[0] = 2;
            }

            String finalFormatText = MsgFormatter.format(speaktext[0]);

            FormBody.Builder form = new FormBody.Builder();
            form.add("text", finalFormatText);
            form.add("speaker", speaker[0]);
            form.add("speed", String.valueOf(speed[0]));
            form.add("pitch", String.valueOf(pitch[0]));
            form.add("format", "mp3");
            if (setEmotion[0]) {
                form.add("emotion", emotion[0]);
                form.add("emotion_level", String.valueOf(emotion_lv[0]));
            }
            this.formatText = finalFormatText;
            this.form = form;
        }
    }
}
