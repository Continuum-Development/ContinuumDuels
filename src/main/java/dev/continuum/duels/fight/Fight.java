package dev.continuum.duels.fight;

import dev.continuum.duels.arena.TemporaryArena;
import dev.continuum.duels.kit.Kit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface Fight<T> {
    void handleDeath(final @NotNull Player dead);

    void leave(final @NotNull Player player);

    boolean endRound(final @NotNull Player winner);

    boolean endFight();

    boolean startRound();

    boolean startFight();

    boolean spectate(final @NotNull Player spectator, final @Nullable Player target);

    boolean stopSpectating(final @NotNull Player spectator);

    boolean ended();

    boolean inProgress();

    boolean ranked();

    boolean friendlyFireAllowed();

    long duration();

    int rounds();

    int round();

    int wins(final @NotNull T team);

    int losses(final @NotNull T team);

    @NotNull
    TemporaryArena arena();

    @NotNull
    Kit kit();

    @NotNull
    Set<Player> players();

    @NotNull
    Set<Player> spectators();

    @NotNull
    Set<T> teams();
}
