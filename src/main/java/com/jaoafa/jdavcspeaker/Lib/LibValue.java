package com.jaoafa.jdavcspeaker.Lib;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.List;

public class LibValue {
    public static final List<String> ignoreContains = new ArrayList<>();
    public static final List<String> ignoreEquals = new ArrayList<>();
    public static JDA jda;
    public static EventWaiter eventWaiter = null;
}
