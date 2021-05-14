package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class DefaultParamsManager {
    private static final File file = new File("user-default-params.json");
    User user;
    VoiceText defaultVoiceText = new VoiceText();

    public DefaultParamsManager(User user) throws VoiceText.WrongException {
        this.user = user;
    }

    JSONObject getData() {
        if (!file.exists()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(String.join("\n", Files.readAllLines(file.toPath())));
        } catch (IOException e) {
            return new JSONObject();
        }
    }

    @Nullable
    JSONObject getDefaultUserParams() {
        return getData().optJSONObject(user.getId());
    }

    public VoiceText getDefaultVoiceText() throws VoiceText.WrongException {
        JSONObject object = getDefaultUserParams();
        if (object == null) return null;
        VoiceText.Emotion emotion = object.has("emotion") ?
            VoiceText.Emotion.valueOf(object.getString("emotion")) : null;
        return defaultVoiceText
            .setSpeaker(VoiceText.Speaker.valueOf(object.getString("speaker")))
            .setSpeed(object.getInt("speed"))
            .setEmotion(emotion)
            .setEmotionLevel(VoiceText.EmotionLevel.valueOf(object.getString("emotionLevel")))
            .setPitch(object.getInt("pitch"));
    }

    public boolean setDefaultVoiceText(VoiceText vt) {
        JSONObject object = new JSONObject();
        if (vt != null) {
            object.put("speaker", vt.getSpeaker().name());
            object.put("speed", vt.getSpeed());
            object.put("emotion", vt.getEmotion() != null ? vt.getEmotion().name() : null);
            object.put("emotionLevel", vt.getEmotionLevel().name());
            object.put("pitch", vt.getPitch());
        } else {
            object = null;
        }

        JSONObject data = getData();
        data.put(user.getId(), object);
        try {
            Files.write(file.toPath(), Collections.singleton(data.toString()));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
