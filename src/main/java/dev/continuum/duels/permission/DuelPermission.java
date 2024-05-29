package dev.continuum.duels.permission;

import org.jetbrains.annotations.NotNull;

public enum DuelPermission {
    ARENA_COMMAND("continuum.duels.commands.arena");

    private final String key;

    DuelPermission(final @NotNull String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
