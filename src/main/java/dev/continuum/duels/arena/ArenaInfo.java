package dev.continuum.duels.arena;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class ArenaInfo {
    private Material icon;
    private String displayName;

    public ArenaInfo(final @NotNull Arena arena) {
        this.icon = Material.BEDROCK;
        this.displayName = StringUtils.capitalize(arena.name());
    }

    @NotNull
    public static ArenaInfo info(final @NotNull Arena arena) {
        return new ArenaInfo(arena);
    }

    @NotNull
    public String displayName() {
        return displayName;
    }

    @NotNull
    @CanIgnoreReturnValue
    public ArenaInfo displayName(final @NotNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    @NotNull
    public Material icon() {
        return icon;
    }

    @NotNull
    @CanIgnoreReturnValue
    public ArenaInfo icon(final @NotNull Material icon) {
        this.icon = icon;
        return this;
    }
}
