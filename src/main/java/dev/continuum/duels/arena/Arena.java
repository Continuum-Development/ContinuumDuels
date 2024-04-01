package dev.continuum.duels.arena;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Files;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Arena {
    private final String name;
    private String displayName;
    private Location cornerOne;
    private Location cornerTwo;
    private Location spawnOne;
    private Location spawnTwo;
    private Location center;
    private final Set<String> linkedPremadeKits = new HashSet<>();
    private Material icon = Material.GRASS_BLOCK;
    private boolean enabled = true;
    private boolean inUse = false;

    public Arena(final @NotNull String name) {
        this.name = name;
        this.displayName = StringUtils.capitalize(name);
    }

    public Arena(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Location cornerOne, final @Nullable Location cornerTwo,
        final @Nullable Location spawnOne, final @Nullable Location spawnTwo,
        final @Nullable Location center,
        final @Nullable Set<String> linkedPremadeKits,
        final @Nullable Material icon,
        final boolean enabled, final boolean inUse
    ) {
        this.name = name;
        this.displayName = StringUtils.capitalize(name);
        if (displayName != null) this.displayName = displayName;
        if (cornerOne != null) this.cornerOne = cornerOne;
        if (cornerTwo != null) this.cornerTwo = cornerTwo;
        if (spawnOne != null) this.spawnOne = spawnOne;
        if (spawnTwo != null) this.spawnTwo = spawnTwo;
        if (center != null) this.center = center;
        if (linkedPremadeKits != null) this.linkedPremadeKits.addAll(linkedPremadeKits);
        if (icon != null) this.icon = icon;
        this.enabled = enabled;
        this.inUse = inUse;
    }

    public Arena(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Location cornerOne, final @Nullable Location cornerTwo,
        final @Nullable Location spawnOne, final @Nullable Location spawnTwo,
        final @Nullable Location center,
        final @Nullable Set<String> linkedPremadeKits,
        final @Nullable Material icon,
        final boolean enabled
    ) {
        this(name, displayName, cornerOne, cornerTwo, spawnOne, spawnTwo, center, linkedPremadeKits, icon, enabled, false);
    }

    @NotNull
    public static Arena arena(final @NotNull String name) {
        return new Arena(name);
    }

    @NotNull
    public static Arena arena(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Location cornerOne, final @Nullable Location cornerTwo,
        final @Nullable Location spawnOne, final @Nullable Location spawnTwo,
        final @Nullable Location center,
        final @Nullable Set<String> linkedPremadeKits,
        final @Nullable Material icon,
        final boolean enabled, final boolean inUse
    ) {
        return new Arena(name, displayName, cornerOne, cornerTwo, spawnOne, spawnTwo, center, linkedPremadeKits, icon, enabled, inUse);
    }

    @NotNull
    public static Arena arena(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Location cornerOne, final @Nullable Location cornerTwo,
        final @Nullable Location spawnOne, final @Nullable Location spawnTwo,
        final @Nullable Location center,
        final @Nullable Set<String> linkedPremadeKits,
        final @Nullable Material icon,
        final boolean enabled
    ) {
        return new Arena(name, displayName, cornerOne, cornerTwo, spawnOne, spawnTwo, center, linkedPremadeKits, icon, enabled);
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String displayName() {
        if (displayName == null) displayName = StringUtils.capitalize(name);
        return displayName;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena displayName(final @Nullable String displayName) {
        if (displayName == null) {
            this.displayName = StringUtils.capitalize(name);
            return this;
        }

        this.displayName = displayName;
        return this;
    }

    @Nullable
    public Location cornerOne() {
        return cornerOne;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena cornerOne(final @Nullable Location cornerOne) {
        this.cornerOne = cornerOne;
        return this;
    }

    @Nullable
    public Location cornerTwo() {
        return cornerTwo;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena cornerTwo(final @Nullable Location cornerTwo) {
        this.cornerTwo = cornerTwo;
        return this;
    }

    @Nullable
    public Location spawnOne() {
        return spawnOne;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena spawnOne(final @Nullable Location spawnOne) {
        this.spawnOne = spawnOne;
        return this;
    }

    @Nullable
    public Location spawnTwo() {
        return spawnTwo;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena spawnTwo(final @Nullable Location spawnTwo) {
        this.spawnTwo = spawnTwo;
        return this;
    }

    @Nullable
    public Location center() {
        return center;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena center(final @Nullable Location center) {
        this.center = center;
        return this;
    }

    @NotNull
    public Set<String> linkedPremadeKits() {
        return linkedPremadeKits;
    }

    @NotNull
    public Material icon() {
        if (icon == null) icon = Material.GRASS_BLOCK;
        return icon;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena icon(final @Nullable Material icon) {
        if (icon == null) {
            this.icon = Material.GRASS_BLOCK;
            return this;
        }

        this.icon = icon;
        return this;
    }

    public boolean enabled() {
        return enabled;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena enabled(final boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean inUse() {
        return inUse;
    }

    @NotNull
    @CanIgnoreReturnValue
    public Arena inUse(final boolean inUse) {
        this.inUse = inUse;
        return this;
    }

    @NotNull
    public File schematic() {
        Files.mkdirs(Files.file("schematics/"));
        return Files.file("schematics", name + ".schem");
    }

    public void save() {
        Files.mkdirs(Files.file("arenas/"));

        final File file = Files.create(Files.file("arenas", name + ".yml"));
        final FileConfiguration config = Files.config(file);

        config.set("name", name());
        config.set("display_name", displayName());

        config.set("corners.one", cornerOne());
        config.set("corners.two", cornerTwo());

        config.set("spawns.one", spawnOne());
        config.set("spawns.two", spawnTwo());

        config.set("center", center());

        config.set("linked_premade_kits", linkedPremadeKits.stream().toList());

        config.set("icon", icon.toString()
            .toLowerCase()
            .replaceAll("_", " ")
            .replaceAll("minecraft:", "")
        );

        config.set("enabled", enabled);

        Files.saveConfig(file, config);
    }

    public void regenerate() {

    }
}
