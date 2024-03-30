package dev.continuum.duels.util;

import dev.manere.utils.misc.ObjectUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class Materials {
    @NotNull
    public static Material parse(@NotNull String raw) {
        raw = raw.toUpperCase();
        raw = raw.replaceAll(" ", "_");
        raw = raw.replaceAll("minecraft:", "");

        return ObjectUtils.defaultIfNull(Material.matchMaterial(raw), Material.GRASS_BLOCK);
    }
}
