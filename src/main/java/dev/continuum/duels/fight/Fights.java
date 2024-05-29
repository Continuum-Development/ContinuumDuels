package dev.continuum.duels.fight;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Fights {
    private static final List<DuelFight> duels = new ArrayList<>();

    public static void duel(final @NotNull DuelFight fight) {
        duels.add(fight);
    }

    @Nullable
    public static DuelFight duel(final @NotNull Player player) {
        for (final DuelFight fight : duels) {
            if (fight.playersAndSpectators().contains(player)) {
                return fight;
            }
        }

        return null;
    }

    @Nullable
    public static DuelFight duelSpectator(final @NotNull Player spectator) {
        for (final DuelFight fight : duels) {
            if (fight.spectators().contains(spectator)) {
                return fight;
            }
        }

        return null;
    }

    @Nullable
    public static DuelFight duelPlayer(final @NotNull Player spectator) {
        for (final DuelFight fight : duels) {
            if (fight.players().contains(spectator)) {
                return fight;
            }
        }

        return null;
    }
}
