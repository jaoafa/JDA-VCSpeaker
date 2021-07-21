package com.jaoafa.jdavcspeaker.Lib;


import static com.jaoafa.jdavcspeaker.Lib.LibTextColor.*;

public class LibFlow {
    private static String actionName;

    private String getPrefix(String symbol, String color) {
        return String.format("|%s%s%s|[%s%s%s] ", color, symbol, RESET, BLUE_BRIGHT, actionName, RESET);
    }

    public LibFlow setName(String name) {
        actionName = name;
        return this;
    }

    public LibFlow header(String name, String... format) {
        System.out.printf("===// %s%s%s //\n", YELLOW, String.format(name, (Object[]) format), RESET);
        return this;
    }

    public LibFlow task(String task, String... format) {
        System.out.println(getPrefix(">", BLUE) + String.format(task, (Object[]) format));
        return this;
    }

    public LibFlow action(String action, String... format) {
        System.out.println(getPrefix("*", PURPLE) + String.format(action, (Object[]) format));
        return this;
    }

    public LibFlow success(String success, String... format) {
        System.out.println(getPrefix("+", GREEN_BRIGHT) + String.format(success, (Object[]) format));
        return this;
    }

    public LibFlow error(String error, String... format) {
        System.out.println(getPrefix("#", RED) + String.format(error, (Object[]) format));
        return this;
    }

    public LibFlow pipe() {
        System.out.printf("|%s|%s|\n", CYAN, RESET);
        return this;
    }
}
