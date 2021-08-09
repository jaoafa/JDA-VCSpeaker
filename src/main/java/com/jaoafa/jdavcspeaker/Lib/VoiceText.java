package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class VoiceText {
    // Default settings
    Speaker speaker = Speaker.HIKARI;
    int speed = 120;
    Emotion emotion = null;
    EmotionLevel emotionLevel = EmotionLevel.NORMAL;
    int pitch = 100;

    /**
     * Initialize Voice Text object
     */
    public VoiceText() {
    }

    /**
     * Initialize Voice Text object from user
     *
     * @param user User
     *
     * @throws WrongException if the default parameters are incorrect
     */
    public VoiceText(User user) throws WrongException {
        DefaultParamsManager dpm = new DefaultParamsManager(user);
        VoiceText vt = dpm.getDefaultVoiceText();
        if (vt == null) return;
        speaker = vt.getSpeaker();
        speed = vt.getSpeed();
        emotion = vt.getEmotion();
        emotionLevel = vt.getEmotionLevel();
        pitch = vt.getPitch();
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    /**
     * Set the speaker
     *
     * @param speaker Speaker
     *
     * @return VoiceText object after the title has been set
     *
     * @throws WrongSpeakerException if the value is incorrect
     */
    @CheckReturnValue
    public VoiceText setSpeaker(Speaker speaker) throws WrongSpeakerException {
        if (speaker == Speaker.__WRONG__) {
            throw new WrongSpeakerException();
        }
        this.speaker = speaker;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    /**
     * Set the speed
     *
     * @param speed Speed (speed >= 50 || speed <= 400)
     *
     * @return VoiceText object after the speed has been set
     *
     * @throws WrongSpeedException if the value is incorrect
     */
    @CheckReturnValue
    public VoiceText setSpeed(int speed) throws WrongSpeedException {
        if (speed < 50 || speed > 400) {
            throw new WrongSpeedException();
        }
        this.speed = speed;
        return this;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    /**
     * Set the emotion
     *
     * @param emotion Emotion
     *
     * @return VoiceText object after the emotion has been set
     *
     * @throws WrongEmotionException if the value is incorrect
     */
    @CheckReturnValue
    public VoiceText setEmotion(Emotion emotion) throws WrongEmotionException {
        if (emotion == Emotion.__WRONG__) {
            throw new WrongEmotionException();
        }
        this.emotion = emotion;
        return this;
    }

    public EmotionLevel getEmotionLevel() {
        return emotionLevel;
    }

    /**
     * Set the emotion level
     *
     * @param emotionLevel EmotionLevel
     *
     * @return VoiceText object after the emotion level has been set
     *
     * @throws WrongEmotionLevelException if the value is incorrect
     */
    @CheckReturnValue
    public VoiceText setEmotionLevel(EmotionLevel emotionLevel) throws WrongEmotionLevelException {
        if (emotionLevel == EmotionLevel.__WRONG__) {
            throw new WrongEmotionLevelException();
        }
        this.emotionLevel = emotionLevel;
        return this;
    }

    public int getPitch() {
        return pitch;
    }

    /**
     * Set the pitch
     *
     * @param pitch Pitch
     *
     * @return VoiceText object after the pitch has been set
     *
     * @throws WrongPitchException if the value is incorrect
     */
    @CheckReturnValue
    public VoiceText setPitch(int pitch) throws WrongPitchException {
        if (pitch < 50 || pitch > 200) {
            throw new WrongPitchException();
        }
        this.pitch = pitch;
        return this;
    }

    /**
     * Parse VoiceText parameters
     *
     * @param content Message string to parse
     *
     * @return VoiceText object after parameters has been set
     *
     * @throws WrongSpeakerException      if the speaker param is incorrect
     * @throws WrongSpeedException        if the speed param is incorrect
     * @throws WrongEmotionException      if the emotion param is incorrect
     * @throws WrongEmotionLevelException if the emotion level param is incorrect
     * @throws WrongPitchException        if the pitch param is incorrect
     */
    @CheckReturnValue
    public VoiceText parseMessage(String content) throws WrongSpeakerException, WrongSpeedException, WrongEmotionException, WrongEmotionLevelException, WrongPitchException {
        Speaker speaker = Arrays.stream(content.split(" "))
            .filter(s -> s.startsWith("speaker:"))
            .map(s -> s.substring("speaker:".length()))
            .map(Speaker::getEnum)
            .findFirst()
            .orElse(null);
        if (speaker == Speaker.__WRONG__) {
            throw new WrongSpeakerException();
        }
        if (speaker != null) this.speaker = speaker;

        Integer speed = Arrays.stream(content.split(" "))
            .filter(s -> s.startsWith("speed:"))
            .map(s -> s.substring("speed:".length()))
            .map(Integer::parseInt)
            .findFirst()
            .orElse(null);
        if (speed != null && (speed < 50 || speed > 400)) {
            throw new WrongSpeedException();
        }
        if (speed != null) this.speed = speed;

        Emotion emotion = Arrays.stream(content.split(" "))
            .filter(s -> s.startsWith("emotion:"))
            .map(s -> s.substring("emotion:".length()))
            .map(Emotion::getEnum)
            .findFirst()
            .orElse(null);
        if (emotion == Emotion.__WRONG__) {
            throw new WrongEmotionException();
        }
        if (emotion != null) {
            this.emotion = emotion;
            EmotionLevel emotionLevel = Arrays.stream(content.split(" "))
                .filter(s -> s.startsWith("emotion_level:"))
                .map(s -> s.substring("emotion_level:".length()))
                .map(EmotionLevel::getEnum)
                .findFirst()
                .orElse(null);
            if (emotionLevel == EmotionLevel.__WRONG__) {
                throw new WrongEmotionLevelException();
            }

            if (emotionLevel != null) this.emotionLevel = emotionLevel;
        }

        Integer pitch = Arrays.stream(content.split(" "))
            .filter(s -> s.startsWith("pitch:"))
            .map(s -> s.substring("pitch:".length()))
            .map(Integer::parseInt)
            .findFirst()
            .orElse(null);
        if (pitch != null && (pitch < 50 || pitch > 200)) {
            throw new WrongPitchException();
        }
        if (pitch != null) this.pitch = pitch;

        return this;
    }

    /**
     * Play speak message
     *
     * @param message   Message object
     * @param speakText Speak message
     */
    public void play(Message message, String speakText) {
        if (speakText.length() == 0) {
            return;
        }

        VoiceText vt;
        try {
            vt = parseMessage(speakText);
        } catch (WrongSpeakerException e) {
            String allowParams = Arrays.stream(VoiceText.Speaker.values())
                .filter(s -> !s.equals(VoiceText.Speaker.__WRONG__))
                .map(Enum::name)
                .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`speaker` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongSpeedException e) {
            message.reply(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`speed` が正しくありません。使用可能なパラメーターは `%s` です。", "50 ～ 400"))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongEmotionException e) {
            String allowParams = Arrays.stream(VoiceText.Emotion.values())
                .filter(s -> !s.equals(VoiceText.Emotion.__WRONG__))
                .map(Enum::name)
                .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`emotion` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongEmotionLevelException e) {
            String allowParams = Arrays.stream(VoiceText.EmotionLevel.values())
                .filter(s -> !s.equals(VoiceText.EmotionLevel.__WRONG__))
                .map(Enum::name)
                .collect(Collectors.joining("`, `"));
            message.reply(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`emotionLevel` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongPitchException e) {
            message.reply(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`pitch` が正しくありません。使用可能なパラメーターは `%s` です。", "50 ～ 200"))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        }
        speaker = vt.getSpeaker();
        speed = vt.getSpeed();
        emotion = vt.getEmotion();
        emotionLevel = vt.getEmotionLevel();
        pitch = vt.getPitch();

        System.out.println(this);

        String formattedText = MsgFormatter.format(speakText);
        String hash = DigestUtils.md5Hex("%s_%s_%d_%s_%s_%d".formatted(speakText,
            speaker.name(),
            speed,
            emotion != null ? emotion.name() : "null",
            emotionLevel != null ? emotionLevel.name() : "null",
            pitch));

        if (new File("./Temp/" + hash + ".mp3").exists()) {
            TrackInfo info = new TrackInfo(message);
            PlayerManager.getINSTANCE().loadAndPlay(info, "./Temp/" + hash + ".mp3");
            return;
        }

        message
            .addReaction("\uD83D\uDCF2") // :calling:
            .queue(null, Throwable::printStackTrace);

        try {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder form = new FormBody.Builder()
                .add("text", formattedText)
                .add("speaker", speaker.name().toLowerCase())
                .add("speed", String.valueOf(speed))
                .add("pitch", String.valueOf(pitch))
                .add("format", "mp3");
            if (emotion != null && emotionLevel != null) {
                form = form.add("emotion", emotion.name().toLowerCase());
                form = form.add("emotion_level", String.valueOf(emotionLevel.getLevel()));
            }

            Request request = new Request.Builder()
                .post(form.build())
                .url("https://api.voicetext.jp/v1/tts")
                .header("Authorization", Credentials.basic(Main.getSpeakToken(), ""))
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
                Files.write(Paths.get("./Temp/" + hash + ".mp3"), body.bytes());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            message
                .removeReaction("\uD83D\uDCF2") // :calling:
                .queue(null, Throwable::printStackTrace);
            TrackInfo info = new TrackInfo(message);
            PlayerManager.getINSTANCE().loadAndPlay(info, "./Temp/" + hash + ".mp3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "VoiceText{" +
            "speaker=" + speaker +
            ", speed=" + speed +
            ", emotion=" + emotion +
            ", emotionLevel=" + emotionLevel +
            ", pitch=" + pitch +
            '}';
    }

    public enum Speaker {
        SHOW,
        HARUKA,
        HIKARI,
        TAKERU,
        SANTA,
        BEAR,
        __WRONG__;

        public static Speaker getEnum(String name) {
            return Arrays.stream(values())
                .filter(speaker -> speaker.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(Speaker.__WRONG__);
        }
    }

    public enum Emotion {
        HAPPINESS("HAPPY"),
        ANGER,
        SADNESS("SAD"),
        __WRONG__;

        String alias = null;

        Emotion() {
        }

        Emotion(String alias) {
            this.alias = alias;
        }

        public static Emotion getEnum(String name) {
            return Arrays.stream(values())
                .filter(emotion ->
                    emotion.name().equalsIgnoreCase(name) ||
                        (emotion.getAlias() != null && emotion.getAlias().equalsIgnoreCase(name)))
                .findFirst()
                .orElse(Emotion.__WRONG__);
        }

        public String getAlias() {
            return alias;
        }
    }

    public enum EmotionLevel {
        LOW(1),
        NORMAL(2),
        HIGH(3),
        SUPER(4),
        __WRONG__(Integer.MAX_VALUE);

        final int lvl;

        EmotionLevel(int lvl) {
            this.lvl = lvl;
        }

        public static EmotionLevel getEnum(String name) {
            return Arrays.stream(values())
                .filter(emotion_level ->
                    emotion_level.name().equalsIgnoreCase(name) ||
                        String.valueOf(emotion_level.getLevel()).equals(name))
                .findFirst()
                .orElse(EmotionLevel.__WRONG__);
        }

        public int getLevel() {
            return lvl;
        }
    }

    public static class WrongException extends Exception {
        public WrongException() {
        }
    }

    public static class WrongSpeakerException extends WrongException {
        public WrongSpeakerException() {
        }
    }

    public static class WrongEmotionException extends WrongException {
        public WrongEmotionException() {
        }
    }

    public static class WrongEmotionLevelException extends WrongException {
        public WrongEmotionLevelException() {
        }
    }

    public static class WrongPitchException extends WrongException {
        public WrongPitchException() {
        }
    }

    public static class WrongSpeedException extends WrongException {
        public WrongSpeedException() {
        }
    }
}
