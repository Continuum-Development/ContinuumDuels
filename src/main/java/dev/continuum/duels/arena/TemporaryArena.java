package dev.continuum.duels.arena;

import dev.continuum.duels.world.DuelWorlds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryArena implements Arena {
    private final String name;
    private final GridPoint grid;
    private final Map<ArenaCorner, Location> corners = new ConcurrentHashMap<>();
    private final Map<ArenaSpawn, Location> spawns = new ConcurrentHashMap<>();
    private final Location center;
    private final ArenaInfo info;
    private int borderSize;

    @SuppressWarnings("DataFlowIssue") // Arena should not have nullables
    public TemporaryArena(final @NotNull PremadeArena arena, final @NotNull GridPoint grid) {
        this.grid = grid;

        this.corners.put(ArenaCorner.ONE, GridPoint.location(arena.corner(ArenaCorner.ONE), grid));
        this.corners.put(ArenaCorner.TWO, GridPoint.location(arena.corner(ArenaCorner.TWO), grid));

        this.spawns.put(ArenaSpawn.ONE, GridPoint.location(arena.spawn(ArenaSpawn.ONE), grid));
        this.spawns.put(ArenaSpawn.TWO, GridPoint.location(arena.spawn(ArenaSpawn.TWO), grid));

        this.center = GridPoint.location(arena.center(), grid);
        this.borderSize = arena.borderSize();

        this.info = arena.info();
        this.name = arena.name();
    }

    @NotNull
    public static TemporaryArena create(final @NotNull PremadeArena arena, final @NotNull GridPoint point) {
        return new TemporaryArena(arena, point);
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
    public Location spawn(final @NotNull ArenaSpawn spawn) {
        return spawns.get(spawn);
    }

    @Nullable
    @Override
    public Location corner(final @NotNull ArenaCorner corner) {
        return corners.get(corner);
    }

    @NotNull
    @Override
    public Arena spawn(final @NotNull ArenaSpawn point, final @NotNull Location spawn) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Arena corner(final @NotNull ArenaCorner point, final @NotNull Location corner) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Arena center(final @NotNull Location center) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException("Attempted to save a TemporaryArena");
    }

    @Override
    public boolean load() {
        throw new UnsupportedOperationException("Attempted to save a TemporaryArena");
    }

    @NotNull
    public GridPoint grid() {
        return grid;
    }

    @Override
    public void delete() {
        final Location cornerOne = corner(ArenaCorner.ONE);
        final Location cornerTwo = corner(ArenaCorner.TWO);

        if (cornerOne == null || cornerTwo == null) throw new UnsupportedOperationException();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world " + DuelWorlds.temporaryArenas().getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 <x> <y> <z>"
            .replaceAll("<x>", String.valueOf(cornerOne.x()))
            .replaceAll("<y>", String.valueOf(cornerOne.y()))
            .replaceAll("<z>", String.valueOf(cornerOne.z()))
        );

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 <x> <y> <z>"
            .replaceAll("<x>", String.valueOf(cornerTwo.x()))
            .replaceAll("<y>", String.valueOf(cornerTwo.y()))
            .replaceAll("<z>", String.valueOf(cornerTwo.z()))
        );

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/set air");

        GridPoints.remove(grid);
    }
}
