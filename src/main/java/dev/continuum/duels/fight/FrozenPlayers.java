package dev.continuum.duels.fight;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FrozenPlayers {
    private static final Set<UUID> players = new HashSet<>();

    public static void freeze(final @NotNull UUID uuid) {
        players.add(uuid);
    }

    public static void freeze(final @NotNull Player player) {
        freeze(player.getUniqueId());
    }

    public static void unfreeze(final @NotNull UUID uuid) {
        players.remove(uuid);
    }

    public static void unfreeze(final @NotNull Player player) {
        unfreeze(player.getUniqueId());
    }

    public static boolean has(final @NotNull UUID uuid) {
        return players.contains(uuid);
    }

    public static boolean has(final @NotNull Player player) {
        return has(player.getUniqueId());
    }
}
