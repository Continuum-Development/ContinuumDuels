package dev.continuum.duels.game;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface GamePlayer {
    @NotNull
    static GamePlayer player(final @NotNull Player player, final @NotNull GameTeam team) {
        return new GamePlayer() {
            @NotNull
            @Override
            public Player player() {
                return player;
            }

            @NotNull
            @Override
            public GameTeam team() {
                return team;
            }
        };
    }

    @NotNull
    Player player();

    @NotNull
    default UUID uuid() {
        return player().getUniqueId();
    }

    @NotNull
    default String name() {
        return player().getName();
    }

    @NotNull
    GameTeam team();
}
