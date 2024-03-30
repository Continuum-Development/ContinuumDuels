package dev.continuum.duels.kit.premade;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Files;
import dev.manere.utils.cachable.Cachable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PremadeKit {
    private final String name;
    private String displayName;
    private Material icon = Material.DIAMOND_SWORD;
    private boolean editable = true;
    private Cachable<Integer, ItemStack> defaultLayout = Cachable.of();

    public PremadeKit(final @NotNull String name) {
        this.name = name;
        this.displayName = StringUtils.capitalize(name);
    }

    public PremadeKit(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Material icon,
        final boolean editable,
        final @Nullable Cachable<Integer, ItemStack> defaultLayout
    ) {
        this.name = name;
        this.displayName = StringUtils.capitalize(name);
        if (displayName != null) this.displayName = displayName;
        if (icon != null) this.icon = icon;
        this.editable = editable;
        if (defaultLayout != null) this.defaultLayout = defaultLayout;
    }

    @NotNull
    public static PremadeKit kit(final @NotNull String name) {
        return new PremadeKit(name);
    }

    @NotNull
    public static PremadeKit kit(
        final @NotNull String name, final @Nullable String displayName,
        final @Nullable Material icon,
        final boolean editable,
        final @Nullable Cachable<Integer, ItemStack> defaultLayout
    ) {
        return new PremadeKit(name, displayName, icon, editable, defaultLayout);
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
    public PremadeKit displayName(final @Nullable String displayName) {
        if (displayName == null) {
            this.displayName = StringUtils.capitalize(name);
            return this;
        }

        this.displayName = displayName;
        return this;
    }

    @NotNull
    public Material icon() {
        if (icon == null) icon = Material.GRASS_BLOCK;
        return icon;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PremadeKit icon(final @Nullable Material icon) {
        if (icon == null) {
            this.icon = Material.GRASS_BLOCK;
            return this;
        }

        this.icon = icon;
        return this;
    }

    public boolean editable() {
        return editable;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PremadeKit editable(final boolean editable) {
        this.editable = editable;
        return this;
    }

    @Nullable
    public Cachable<Integer, ItemStack> defaultLayout() {
        return defaultLayout;
    }

    @NotNull
    public PremadeKit defaultLayout(final @Nullable Cachable<Integer, ItemStack> defaultLayout) {
        this.defaultLayout = defaultLayout;
        return this;
    }

    public void save() {
        Files.mkdirs(Files.file("kits/"));

        final File file = Files.create(Files.file("kits", name + ".yml"));
        final FileConfiguration config = Files.config(file);

        config.set("name", name());
        config.set("display_name", displayName());

        config.set("icon", icon.toString()
            .toLowerCase()
            .replaceAll("_", " ")
            .replaceAll("minecraft:", "")
        );

        config.set("editable", editable);

        ConfigurationSection section = config.getConfigurationSection("contents");
        if (section == null) section = config.createSection("contents");

        section.set("data", defaultLayout.snapshot().asMap());

        Files.saveConfig(file, config);
    }
}
