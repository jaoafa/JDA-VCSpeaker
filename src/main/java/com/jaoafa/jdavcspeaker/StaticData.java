package com.jaoafa.jdavcspeaker;

import net.dv8tion.jda.api.JDA;

import java.util.HashMap;
import java.util.Map;

public class StaticData {
    public static JDA jda;
    public static final Map<String, String> aliasMap = new HashMap<>();
    public static final Map<String, String> ignoreMap = new HashMap<>();
}
