package dev.continuum.duels.game;

import dev.manere.utils.cachable.Cachable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Games {
    private static final List<Game<?>> games = new ArrayList<>();
    private static final Cachable<Player, Game<?>> spectators = Cachable.of();

    public static void add(final @NotNull Game<?> game) {
        games.add(game);
    }

    public static void remove(final @NotNull Game<?> game) {
        games.remove(game);
    }

    public static boolean has(final @NotNull Game<?> game) {
        return games.contains(game);
    }

    @NotNull
    public static List<Game<?>> games() {
        return games;
    }

    @Nullable
    public static <P extends GamePlayer> Game<?> game(final @NotNull P player) {
        for (final Game<?> game : games) {
            final Set<? extends GamePlayer> players = game.players();

            for (final GamePlayer gamePlayer : players) {
                if (gamePlayer.player().getUniqueId().equals(player.player().getUniqueId())) return game;
            }
        }

        return null;
    }

    @Nullable
    public static Game<?> game(final @NotNull Player player) {
        for (final Game<?> game : games) {
            final Set<? extends GamePlayer> players = game.players();

            for (final GamePlayer gamePlayer : players) {
                if (gamePlayer.player().getUniqueId().equals(player.getUniqueId())) return game;
            }
        }

        return null;
    }

    @NotNull
    public static Cachable<Player, Game<?>> spectators() {
        return spectators;
    }
}
