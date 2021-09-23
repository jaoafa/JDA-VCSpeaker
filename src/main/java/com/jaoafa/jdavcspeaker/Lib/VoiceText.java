package com.jaoafa.jdavcspeaker.Lib;

import com.jaoafa.jdavcspeaker.Main;
import com.jaoafa.jdavcspeaker.Player.GuildMusicManager;
import com.jaoafa.jdavcspeaker.Player.PlayerManager;
import com.jaoafa.jdavcspeaker.Player.TrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
import java.util.*;
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
     * @param speakFromType TrackInfo.SpeakFromType 読み上げ種別
     * @param message       Message object
     * @param speakText     Speak message
     */
    public void play(TrackInfo.SpeakFromType speakFromType, Message message, String speakText) {
        if (speakText.length() == 0) {
            return;
        }
        System.out.printf("[VoiceText.play] %s by %s (%s)%n", message.getContentDisplay().length() >= 10 ? message.getContentDisplay().substring(0, 10) : message.getContentDisplay(), message.getAuthor().getAsTag(), speakFromType.name());

        VoiceText vt;
        try {
            vt = parseMessage(speakText);
        } catch (WrongSpeakerException e) {
            String allowParams = Arrays.stream(VoiceText.Speaker.values())
                .filter(s -> !s.equals(VoiceText.Speaker.__WRONG__))
                .map(Enum::name)
                .collect(Collectors.joining("`, `"));
            message.replyEmbeds(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`speaker` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongSpeedException e) {
            message.replyEmbeds(new EmbedBuilder()
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
            message.replyEmbeds(new EmbedBuilder()
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
            message.replyEmbeds(new EmbedBuilder()
                .setTitle(":bangbang: メッセージパラメーターが不正")
                .setDescription(String.format("`emotionLevel` が正しくありません。使用可能なパラメーターは `%s` です。", allowParams))
                .setColor(LibEmbedColor.error)
                .build()).queue();
            return;
        } catch (WrongPitchException e) {
            message.replyEmbeds(new EmbedBuilder()
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
        String hash = DigestUtils.md5Hex("%s_%s_%d_%s_%s_%d".formatted(formattedText,
            speaker.name(),
            speed,
            emotion != null ? emotion.name() : "null",
            emotionLevel != null ? emotionLevel.name() : "null",
            pitch));

        if (new File("./Temp/" + hash + ".mp3").exists()) {
            filteringQueue(speakFromType, message);
            TrackInfo info = new TrackInfo(speakFromType, message);
            PlayerManager.getINSTANCE().loadAndPlay(info, "./Temp/" + hash + ".mp3");
            return;
        }

        message
            .addReaction("\uD83D\uDC40") // :eyes:
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
            filteringQueue(speakFromType, message);
            TrackInfo info = new TrackInfo(speakFromType, message);
            PlayerManager.getINSTANCE().loadAndPlay(info, "./Temp/" + hash + ".mp3");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void filteringQueue(TrackInfo.SpeakFromType speakFromType, Message message) {
        GuildMusicManager musicManager = PlayerManager.getINSTANCE().getGuildMusicManager(message.getGuild());
        Map<TrackInfo.SpeakFromType, List<TrackInfo.SpeakFromType>> filterRules = new HashMap<>();
        // VC退出時、参加・移動メッセージ読み上げを削除する
        filterRules.put(TrackInfo.SpeakFromType.QUITED_VC,
            Arrays.asList(TrackInfo.SpeakFromType.JOINED_VC, TrackInfo.SpeakFromType.MOVED_VC));
        // GoLive終了時、GoLive開始読み上げを削除する
        filterRules.put(TrackInfo.SpeakFromType.ENDED_GOLIVE,
            Arrays.asList(TrackInfo.SpeakFromType.STARTED_GOLIVE, TrackInfo.SpeakFromType.ENDED_GOLIVE));
        // タイトル変更時、タイトル変更通知読み上げを削除する（最新変更のみ通知）
        filterRules.put(TrackInfo.SpeakFromType.CHANGED_TITLE,
            Collections.singletonList(TrackInfo.SpeakFromType.CHANGED_TITLE));

        if (!filterRules.containsKey(speakFromType)) {
            return;
        }
        List<TrackInfo.SpeakFromType> filterRule = filterRules.get(speakFromType);

        // キューにあるトラックがフィルタルールに合致するか
        musicManager.scheduler.queue.removeIf(track -> {
            if (!(track.getUserData() instanceof TrackInfo info)) {
                return false;
            }
            return filterRule.contains(info.getSpeakFromType()) && info.getUser().getIdLong() == message.getAuthor().getIdLong();
        });
        // 再生中のトラックがフィルタルールに合致するか
        AudioTrack playingTrack = musicManager.scheduler.player.getPlayingTrack();
        if (playingTrack != null &&
            playingTrack.getUserData() instanceof TrackInfo info &&
            filterRule.contains(info.getSpeakFromType()) &&
            info.getUser().getIdLong() == message.getAuthor().getIdLong()) {
            musicManager.scheduler.nextTrack();
        }
    }

    @Override
    public String toString() {
        return """
            VoiceText{
            speaker=%s
            , speed=%s
            , emotion=%s
            , emotionLevel=%s
            , pitch=%s
            }
            """.formatted(speaker,speed,emotion,emotionLevel,pitch);
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
