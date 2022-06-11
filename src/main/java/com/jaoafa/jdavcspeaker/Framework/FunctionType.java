package com.jaoafa.jdavcspeaker.Framework;

import java.util.Arrays;

public enum FunctionType {
    Command,Action,Unknown;

    public static FunctionType get(String name) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(name))
            .findFirst()
            .orElse(FunctionType.Unknown);
    }
}
