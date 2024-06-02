package dev.continuum.duels.kit;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.PremadeArenas;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.MaterialUtils;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.library.Utils;
import dev.manere.utils.misc.ObjectUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PremadeKit implements Kit {
    private KitContents contents = KitContents.empty();
    private Elements<Arena> arenas = Elements.of(Arena.class);
    private final String name;
    private Material icon = Material.DIAMOND_SWORD;
    private String displayName = null;

    public PremadeKit(final @NotNull String name) {
        this.name = name;
    }

    @NotNull
    public static PremadeKit kit(final @NotNull String name) {
        return new PremadeKit(name);
    }

    @NotNull
    @Override
    public String displayName() {
        if (displayName != null) return displayName;

        return Kit.super.displayName();
    }

    @Override
    public boolean premade() {
        return true;
    }

    @Override
    public boolean custom() {
        return false;
    }

    @NotNull
    @Override
    public KitContents contents() {
        return contents;
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public Material icon() {
        return icon;
    }

    @NotNull
    @Override
    public Elements<Arena> arenas() {
        return arenas;
    }

    @NotNull
    @Override
    public Kit contents(final @NotNull KitContents contents) {
        this.contents = contents;
        return this;
    }

    @NotNull
    @Override
    public Kit icon(final @NotNull Material icon) {
        this.icon = icon;
        return this;
    }

    @NotNull
    @Override
    public Kit displayName(final @NotNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    @NotNull
    @Override
    public Kit arenas(final @NotNull Elements<Arena> arenas) {
        this.arenas = arenas;
        return this;
    }

    @NotNull
    @Override
    public Kit addArenas(final @NotNull Arena @NotNull ... arenas) {
        this.arenas.elements(arenas);
        return this;
    }

    @NotNull
    @Override
    public Kit addArenas(final @NotNull Elements<Arena> arenas) {
        this.arenas.elements(arenas.elements());
        return this;
    }

    @Override
    public boolean save() {
        final File file = Files.create(new File(
            Files.mkdirs(Files.file("premade_kits/")),
            "<name>.yml"
                .replaceAll("<name>", name)
        ));

        final FileConfiguration configuration = Files.config(file);

        final List<String> rawArenas = new ArrayList<>();

        for (final Arena arena : arenas) {
            rawArenas.add(arena.name());
        }

        configuration.set("__name__", name);
        configuration.set("display_name", displayName());

        configuration.set("arenas", rawArenas);
        configuration.set("icon", MaterialUtils.prettify(icon));

        final ConfigurationSection contentsSection = ObjectUtils.defaultIfNull(
            configuration.getConfigurationSection("contents"),
            configuration.createSection("contents")
        );

        contentsSection.set("data", contents.contents().snapshot().asMap());

        return Files.saveConfig(file, configuration);
    }

    @Override
    public boolean load() {
        final File file = Files.create(new File(
            Files.mkdirs(Files.file("premade_kits/")),
            "<name>.yml"
                .replaceAll("<name>", name)
        ));

        final FileConfiguration configuration = Files.config(file);

        this.icon = MaterialUtils.parse((String) configuration.get("icon", "diamond sword"));

        this.displayName = (String) configuration.get("display_name");

        final List<String> rawArenas = configuration.getStringList("arenas");

        final Elements<Arena> arenas = Elements.of();

        for (final String rawArena : rawArenas) {
            arenas.element(PremadeArenas.arena(rawArena));
        }

        this.arenas = arenas;

        final Cachable<Integer, ItemStack> contents = Cachable.of();

        final ConfigurationSection contentsSection = ObjectUtils.defaultIfNull(
            configuration.getConfigurationSection("contents"),
            configuration.createSection("contents")
        );

        final Object contentsData = contentsSection.get("data");

        if (contentsData instanceof MemorySection section) {
            for (final String key : section.getKeys(false)) {
                contents.cache(
                    Integer.parseInt(key),
                    Objects.requireNonNullElse(
                        contentsSection.getItemStack(key),
                        new ItemStack(Material.AIR)
                    )
                );
            }
        }

        this.contents = KitContents.create(contents);
        return true;
    }

    @Override
    public void delete() {
        final File file = Files.create(new File(
            Files.mkdirs(Files.file("premade_kits/")),
            "<name>.yml"
                .replaceAll("<name>", name)
        ));

        if (!file.delete()) {
            Utils.plugin().getLogger().severe(
                "Failed to delete " + file.getPath()
            );
            return;
        }

        new PremadeKits().cached().del(this);
    }
}
