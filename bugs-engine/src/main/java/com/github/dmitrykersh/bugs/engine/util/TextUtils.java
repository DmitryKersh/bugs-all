package com.github.dmitrykersh.bugs.engine.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtils {
    public static String toOrdinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }
}
