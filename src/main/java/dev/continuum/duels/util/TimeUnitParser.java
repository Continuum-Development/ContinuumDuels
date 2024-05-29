package dev.continuum.duels.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class TimeUnitParser {
    @NotNull
    public static TimeUnit parse(final @NotNull String text) {
        return switch (text) {
            case "hours", "hour", "h" -> TimeUnit.HOURS;
            case "minutes", "minute", "min", "m" -> TimeUnit.MINUTES;
            default -> TimeUnit.SECONDS;
        };
    }
}
