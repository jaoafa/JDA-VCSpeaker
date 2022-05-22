package com.jaoafa.jdavcspeaker.Lib;

import net.dv8tion.jda.api.entities.User;

public record UserVoiceTextResult(VoiceText vt, boolean isReset) {
    public VoiceText getVoiceText() {
        return vt;
    }

    public static UserVoiceTextResult getUserVoiceText(User user) {
        try {
            return new UserVoiceTextResult(new VoiceText(user), false);
        } catch (VoiceText.WrongException e) {
            new DefaultParamsManager(user).setDefaultVoiceText(null);
            return new UserVoiceTextResult(new VoiceText(), true);
        }
    }
}