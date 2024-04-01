package dev.continuum.duels.game;

import dev.continuum.duels.arena.Arena;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.model.Tuple;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public interface Game<K> {
    void handleDeath(final @NotNull GamePlayer dead);

    void leave(final @NotNull GamePlayer player);

    boolean end();

    boolean start();

    boolean spectate(final @NotNull Player spectator, final @Nullable Player target);

    boolean stopSpectating(final @NotNull Player spectator);

    boolean ended();

    boolean inProgress();

    boolean friendlyFireAllowed();

    boolean ranked();

    long duration();

    int rounds();

    int wins(final @NotNull GameTeam team);

    @NotNull
    Cachable<GameTeam, Integer> wins();

    int losses(final @NotNull GameTeam team);

    @NotNull
    Cachable<GameTeam, Integer> losses();

    @NotNull
    Tuple<Integer, Integer> winsAndLosses(final @NotNull GameTeam team);

    @NotNull
    Cachable<GameTeam, Tuple<Integer, Integer>> winsAndLosses();

    @NotNull
    Arena arena();

    @NotNull
    K kit();

    @NotNull
    default Set<? extends GamePlayer> players() {
        final Set<? extends GameTeam> teams = teams();
        final Set<GamePlayer> players = new HashSet<>();

        for (final GameTeam team : teams) {
            players.addAll(team.players());
        }

        return players;
    }

    @NotNull
    Set<? extends GameTeam> teams();
}
