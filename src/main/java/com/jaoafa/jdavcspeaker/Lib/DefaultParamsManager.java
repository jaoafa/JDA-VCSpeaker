package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class DefaultParamsManager {
    private static final LibFiles.VFile file = LibFiles.VFile.USER_DEFAULT_PARAMS;
    final User user;
    final VoiceText defaultVoiceText = new VoiceText();

    public DefaultParamsManager(User user) {
        this.user = user;
    }

    JSONObject getData() {
        return file.readJSONObject(new JSONObject());
    }

    @Nullable
    JSONObject getDefaultUserParams() {
        return getData().optJSONObject(user.getId());
    }

    @Nullable
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
        return file.write(data);
    }
}
