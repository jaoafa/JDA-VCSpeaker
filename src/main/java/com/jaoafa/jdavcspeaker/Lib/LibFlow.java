package com.jaoafa.jdavcspeaker.Lib;


import javax.annotation.CheckReturnValue;

import static com.jaoafa.jdavcspeaker.Lib.LibTextColor.*;

public record LibFlow(String actionName) {
    @CheckReturnValue
    public LibFlow {
    }

    private String getPrefix(String symbol, String color) {
        return String.format("|%s%s%s|[%s%s%s] ", color, symbol, RESET, BLUE_BRIGHT, actionName, RESET);
    }

    public void header(String name, String... format) {
        System.out.println("===// " + YELLOW + name.formatted((Object[]) format) + RESET + " //===");
    }

    public void task(String task, String... format) {
        System.out.println(getPrefix(">", BLUE) + task.formatted((Object[]) format));
    }

    public void action(String action, String... format) {
        System.out.println(getPrefix("*", PURPLE) + action.formatted((Object[]) format));
    }

    public void success(String success, String... format) {
        System.out.println(getPrefix("+", GREEN_BRIGHT) + success.formatted((Object[]) format));
    }

    public void error(String error, String... format) {
        System.out.println(getPrefix("#", RED) + error.formatted((Object[]) format));
    }

    public void pipe() {
        System.out.println("|" + CYAN + "|" + RESET + "|");
    }
}
