package com.jaoafa.jdavcspeaker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StaticData {
    public static long textChannelId = 623153228267388958L;
    @Nullable
    public static TextChannel textChannel = null;
    public static JDA jda;
    public static Map<String, String> aliasMap = new HashMap<>();
    public static Map<String, String> ignoreMap = new HashMap<>();
}
