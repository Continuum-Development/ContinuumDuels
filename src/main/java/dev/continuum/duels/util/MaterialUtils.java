package dev.continuum.duels.util;

import dev.manere.utils.misc.ObjectUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialUtils {
    @NotNull
    public static Material parse(final @NotNull String value) {
        return ObjectUtils.defaultIfNull(Material.matchMaterial(value
            .toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("minecraft:", "")
        ), Material.GRASS_BLOCK);
    }

    @NotNull
    public static String prettify(final @NotNull Material value) {
        return value.toString()
            .toLowerCase()
            .replaceAll("_", " ");
    }

    @Nullable
    public static Material parseOrNull(final @NotNull String value) {
        return Material.matchMaterial(value
            .toUpperCase()
            .replaceAll(" ", "_")
            .replaceAll("minecraft:", "")
        );
    }
}
