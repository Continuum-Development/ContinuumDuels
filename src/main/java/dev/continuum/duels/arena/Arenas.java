package dev.continuum.duels.arena;

import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.Materials;
import dev.manere.utils.command.impl.suggestions.Suggestion;
import dev.manere.utils.command.impl.suggestions.Suggestions;
import dev.manere.utils.location.LocationUtils;
import dev.manere.utils.scheduler.Schedulers;
import dev.manere.utils.server.Servers;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class Arenas {
    private static final Set<Arena> arenas = new HashSet<>();

    public static void start() {
        Schedulers.async().execute(() -> {
            final File directory = Files.mkdirs(Files.file("arenas/"));
            final File[] children = directory.listFiles();
            if (children == null) return;

            for (final File child : children) {
                final FileConfiguration config = Files.config(child);

                final String name = config.getString("name", child.getName()
                    .replaceAll(".yml", "")
                    .replaceAll(".yaml", "")
                );

                final String displayName = config.getString("display_name", StringUtils.capitalize(
                    name
                ));

                final Location cornerOne = config.getLocation("corners.one");
                final Location cornerTwo = config.getLocation("corners.two");

                final Location spawnOne = config.getLocation("spawns.one");
                final Location spawnTwo = config.getLocation("spawns.two");

                final Location center = config.getLocation("center");

                final Set<String> linkedPremadeKits = new HashSet<>(config.getStringList("linked_premade_kits"));
                final String rawIcon = config.getString("icon", "grass block");
                final Material icon = Materials.parse(rawIcon);


                final boolean enabled = config.getBoolean("enabled", false);

                final Arena arena = Arena.arena(name)
                    .displayName(displayName)
                    .cornerOne(cornerOne)
                    .cornerTwo(cornerTwo)
                    .spawnOne(spawnOne)
                    .spawnTwo(spawnTwo)
                    .center(center)
                    .icon(icon)
                    .enabled(enabled)
                    .inUse(false);

                arena.linkedPremadeKits().addAll(linkedPremadeKits);

                arenas.add(arena);
            }
        });
    }

    @NotNull
    public static Suggestions suggestions() {
        final Suggestions suggestions = Suggestions.of();

        for (final String name : names()) {
            suggestions.add(Suggestion.text(name));
        }

        return suggestions;
    }

    @Nullable
    public static Arena arena(final @NotNull Player player) {
        for (final Arena arena : Arenas.all()) {
            final Location toCheck = player.getLocation();

            final Location one = arena.cornerOne();
            final Location two = arena.cornerTwo();

            if (one == null || two == null) continue;

            if (LocationUtils.locIsBetween(one, two, toCheck)) return arena;
        }

        return null;
    }

    public static boolean insideAny(final @NotNull Player player) {
        return arena(player) != null;
    }

    public static boolean inside(final @NotNull Player player, final @NotNull Arena arena) {
        final Arena nullable = arena(player);
        return nullable != null && nullable.name().equals(arena.name());
    }

    @NotNull
    public static List<? extends Player> inside(final @NotNull Arena arena) {
        final List<Player> players = new ArrayList<>();
        for (final Player player : Servers.online()) if (inside(player, arena)) players.add(player);
        return players;
    }

    public static void stop() {
        for (final Arena arena : arenas) {
            arena.save();
        }
    }

    @NotNull
    public static Set<String> names() {
        final Set<String> names = new HashSet<>();

        for (final @NotNull Arena arena : arenas) {
            names.add(arena.name());
        }

        return Collections.unmodifiableSet(names);
    }

    @Nullable
    public static Arena arena(final @NotNull String name) {
        for (Arena arena : arenas) if (arena.name().equalsIgnoreCase(name)) return arena;
        return null;
    }

    @NotNull
    public static Set<Arena> all() {
        return Collections.unmodifiableSet(arenas);
    }

    public static void delete(final @NotNull Arena arena) {
        arenas.remove(arena);
    }

    public static void create(final @NotNull Arena arena) {
        if (arenas.contains(arena)) {
            arena.save();
            return;
        }

        arena.save();
        arenas.add(arena);
    }
}
