package dev.continuum.duels.world;

import dev.continuum.duels.config.ConfigHandler;
import dev.manere.utils.library.Utils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class DuelWorlds {
    @NotNull
    public static World temporaryArenas() {
        final String name = ConfigHandler.value(String.class, "temporary_arenas.world");
        final World world = Bukkit.getServer().getWorld(name);
        return world != null ? world : newWorld(name);
    }

    @NotNull
    public static World newWorld(final @NotNull String name) {
        return error(WorldCreator.name(name)
            .biomeProvider(new BiomeProvider() {
                @NotNull
                @Override
                public Biome getBiome(final @NotNull WorldInfo worldInfo, final int x, final int y, final int z) {
                    return Biome.THE_VOID;
                }

                @NotNull
                @Override
                public List<Biome> getBiomes(final @NotNull WorldInfo worldInfo) {
                    return List.of(Biome.THE_VOID);
                }
            })
            .generator(new ChunkGenerator() {
                @NotNull
                @Override
                public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
                    return new Location(world, 0, 64, 0);
                }
            })
            .generatorSettings("{\"\"biome\"\": \"\"minecraft:plains\"\",\"\"layers\"\": []}}")
            .environment(World.Environment.NORMAL)
            .generateStructures(false)
            .type(WorldType.FLAT)
            .createWorld()
        );
    }

    @NotNull
    private static World error(final @Nullable World world) {
        if (world == null) {
            Utils.plugin().getLogger().log(Level.SEVERE, "Failed to create world! ${text}..."
                .replaceAll("\\$\\{text}", "World is corrupted. Please try again later.")
            );
            throw new RuntimeException();
        }

        return world;
    }
}
