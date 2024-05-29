package dev.continuum.duels.arena;

import dev.continuum.duels.config.ConfigHandler;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record GridPoint(int x, int y) {
    @NotNull
    public static GridPoint point(final int x, final int y) {
        return new GridPoint(x, y);
    }

    public static int distanceBetweenArenas() {
        return ConfigHandler.value(Integer.class, "temporary_arenas.distance_between_arenas");
    }

    @NotNull
    public static BoundingBox box(final @NotNull BoundingBox box, final @NotNull GridPoint grid) {
        double xMin = box.getMinX() + (grid.x * distanceBetweenArenas());
        double yMin = box.getMinY();
        double zMin = box.getMinZ() + (grid.y * distanceBetweenArenas());

        double xMax = box.getMaxX() + (grid.x * distanceBetweenArenas());
        double yMax = box.getMaxY();
        double zMax = box.getMaxZ() + (grid.y * distanceBetweenArenas());

        return BoundingBox.of(new Vector(xMin, yMin, zMin), new Vector(xMax, yMax, zMax));
    }

    @NotNull
    public static Location location(final @NotNull Location location, final @NotNull GridPoint grid) {
        return location.clone().add(grid.x * distanceBetweenArenas(), 0.0, grid.y + distanceBetweenArenas());
    }

}
