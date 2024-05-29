package dev.continuum.duels.arena;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Savable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Arena extends Savable {
    @NotNull
    String name();

    @NotNull
    ArenaInfo info();

    @Nullable
    Location spawn(final @NotNull ArenaSpawn point);

    @Nullable
    Location corner(final @NotNull ArenaCorner point);

    @NotNull
    @CanIgnoreReturnValue
    Arena spawn(final @NotNull ArenaSpawn point, final @NotNull Location spawn);

    @NotNull
    @CanIgnoreReturnValue
    Arena corner(final @NotNull ArenaCorner point, final @NotNull Location corner);

    @NotNull
    @CanIgnoreReturnValue
    Arena center(final @NotNull Location center);

    @NotNull
    @CanIgnoreReturnValue
    Arena borderSize(final @Nullable Integer borderSize);

    int borderSize();

    @Nullable
    Location center();
}
