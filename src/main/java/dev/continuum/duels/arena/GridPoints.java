package dev.continuum.duels.arena;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GridPoints {
    private static final List<GridPoint> generated = new ArrayList<>();

    @NotNull
    public static GridPoint generate() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        final int x = random.nextInt();
        final int y = random.nextInt();

        final GridPoint point = GridPoint.point(x, y);

        if (generated.contains(point)) return generate();

        generated.add(point);
        return point;
    }

    public static void remove(final @NotNull GridPoint grid) {
        generated.remove(grid);
    }
}
