package dev.continuum.duels.arena;

import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.Materials;
import dev.manere.utils.scheduler.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
                arena.start();
            }
        });
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
