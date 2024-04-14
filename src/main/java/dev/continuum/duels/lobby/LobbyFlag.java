package dev.continuum.duels.lobby;

import org.jetbrains.annotations.NotNull;

public enum LobbyFlag {
    TELEPORT_ON_JOIN("teleport_on_join");

    private final String key;

    LobbyFlag(final @NotNull String key) {
        this.key = key;
    }

    @NotNull
    public String key() {
        return key;
    }
}
