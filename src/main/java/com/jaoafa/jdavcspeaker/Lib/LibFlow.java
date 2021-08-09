package com.jaoafa.jdavcspeaker.Lib;


import javax.annotation.CheckReturnValue;
import java.util.LinkedList;

import static com.jaoafa.jdavcspeaker.Lib.LibTextColor.*;

public class LibFlow {
    private static String actionName;
    private final LinkedList<String> messages = new LinkedList<>();

    private String getPrefix(String symbol, String color) {
        return String.format("|%s%s%s|[%s%s%s] ", color, symbol, RESET, BLUE_BRIGHT, actionName, RESET);
    }

    @CheckReturnValue
    public LibFlow setName(String name) {
        actionName = name;
        return this;
    }

    @CheckReturnValue
    public LibFlow header(String name, String... format) {
        messages.add("===// " + YELLOW + name.formatted((Object[]) format) + RESET + " //===");
        return this;
    }

    @CheckReturnValue
    public LibFlow task(String task, String... format) {
        messages.add(getPrefix(">", BLUE) + task.formatted((Object[]) format));
        return this;
    }

    @CheckReturnValue
    public LibFlow action(String action, String... format) {
        messages.add(getPrefix("*", PURPLE) + action.formatted((Object[]) format));
        return this;
    }

    @CheckReturnValue
    public LibFlow success(String success, String... format) {
        messages.add(getPrefix("+", GREEN_BRIGHT) + success.formatted((Object[]) format));
        return this;
    }

    @CheckReturnValue
    public LibFlow error(String error, String... format) {
        messages.add(getPrefix("#", RED) + error.formatted((Object[]) format));
        return this;
    }

    @CheckReturnValue
    public LibFlow pipe() {
        messages.add("|" + CYAN + "|" + RESET + "|");
        return this;
    }

    public void run() {
        for (String message : messages) {
            System.out.println(message);
        }
    }
}
