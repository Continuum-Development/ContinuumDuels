package dev.continuum.duels.game;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public interface GameTeam {
    @NotNull
    static GameTeam team(final @NotNull String name, final @NotNull String color, final @NotNull GameTeam.PlayerSet set) {
        return new GameTeam() {
            @NotNull
            @Override
            public String name() {
                return name;
            }

            @NotNull
            @Override
            public Set<? extends GamePlayer> players() {
                return set.set(this, new HashSet<>());
            }

            @NotNull
            @Override
            public String color() {
                return color;
            }
        };
    }

    @NotNull
    String name();

    @NotNull
    Set<? extends GamePlayer> players();

    @NotNull
    default String color() {
        return "<white>";
    }

    interface PlayerSet {
        @NotNull
        Set<? extends GamePlayer> set(final @NotNull GameTeam team, final @NotNull Set<GamePlayer> set);
    }
}