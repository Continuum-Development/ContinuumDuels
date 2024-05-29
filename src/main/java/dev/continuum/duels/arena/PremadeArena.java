package dev.continuum.duels.arena;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.MaterialUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PremadeArena implements Arena {
    private final String name;
    private final ArenaInfo info = ArenaInfo.info(this);
    private final Map<ArenaSpawn, Location> spawns = new ConcurrentHashMap<>();
    private final Map<ArenaCorner, Location> corners = new ConcurrentHashMap<>();
    private Location center;
    private int borderSize;

    public PremadeArena(final @NotNull String name) {
        this.name = name;
    }

    @NotNull
    @CanIgnoreReturnValue
    public static PremadeArena arena(final @NotNull String name) {
        return new PremadeArena(name);
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public ArenaInfo info() {
        return info;
    }

    @Nullable
    @Override
    public Location spawn(final @NotNull ArenaSpawn point) {
        return spawns.get(point);
    }

    @Nullable
    @Override
    public Location corner(final @NotNull ArenaCorner point) {
        return corners.get(point);
    }

    @NotNull
    @Override
    public Arena spawn(final @NotNull ArenaSpawn point, final @NotNull Location spawn) {
        this.spawns.put(point, spawn);
        return this;
    }

    @NotNull
    @Override
    public Arena corner(final @NotNull ArenaCorner point, final @NotNull Location corner) {
        this.corners.put(point, corner);
        return this;
    }

    @NotNull
    @Override
    public Arena center(final @NotNull Location center) {
        this.center = center;
        return this;
    }

    @NotNull
    @Override
    public Arena borderSize(final @Nullable Integer borderSize) {
        if (borderSize == null) return borderSize(-1);
        this.borderSize = borderSize;
        return this;
    }

    @Override
    public int borderSize() {
        return borderSize;
    }

    @Nullable
    @Override
    public Location center() {
        return center;
    }

    @Override
    public boolean save() {
        final File file = Files.create(new File(
            Files.mkdirs(Files.file("arenas/")),
            "<name>.yml"
                .replaceAll("<name>", name)
        ));

        final FileConfiguration config = Files.config(file);

        config.set("__name__", name);
        config.set("info.icon", MaterialUtils.prettify(info.icon()));
        config.set("info.display_name", info.displayName());
        config.set("corners.one", corners.get(ArenaCorner.ONE));
        config.set("corners.two", corners.get(ArenaCorner.TWO));
        config.set("spawns.one", spawns.get(ArenaSpawn.ONE));
        config.set("spawns.two", spawns.get(ArenaSpawn.TWO));
        config.set("center", center);
        config.set("border_size", borderSize());

        return true;
    }

    @Override
    public boolean load() {

        return true;
    }

    @Override
    public void delete() {

    }
}
