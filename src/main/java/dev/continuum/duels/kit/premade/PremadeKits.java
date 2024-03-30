package dev.continuum.duels.kit.premade;

import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.Materials;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.scheduler.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PremadeKits {
    private static final Set<PremadeKit> kits = new HashSet<>();

    @SuppressWarnings("unchecked")
    public static void start() {
        Schedulers.async().execute(() -> {
            final File directory = Files.mkdirs(Files.file("kits/"));
            final File[] children = directory.listFiles();
            if (children == null) return;

            for (final File child : children) {
                final FileConfiguration config = Files.config(child);

                final String name = config.getString("name", child.getName()
                    .replaceAll(".yml", "")
                    .replaceAll(".yaml", "")
                );

                final String displayName = config.getString("display_name", StringUtils.capitalize(
                    name
                ));

                final String rawIcon = config.getString("icon", "grass block");
                final Material icon = Materials.parse(rawIcon);

                final boolean editable = config.getBoolean("editable", true);

                final ConfigurationSection section = config.getConfigurationSection("contents");
                if (section == null) {
                    final PremadeKit kit = PremadeKit.kit(name)
                        .displayName(displayName)
                        .defaultLayout(null)
                        .editable(editable)
                        .icon(icon);

                    kits.add(kit);
                    continue;
                }

                final Object raw = section.get("data");
                if (raw == null) {
                    final PremadeKit kit = PremadeKit.kit(name)
                        .displayName(displayName)
                        .defaultLayout(null)
                        .editable(editable)
                        .icon(icon);

                    kits.add(kit);
                    continue;
                }

                Cachable<Integer, ItemStack> data = Cachable.of();

                if (raw instanceof Map<?, ?> map) {
                    data = Cachable.of((Map<Integer, ItemStack>) map);
                } else if (raw instanceof MemorySection memorySection) {
                    final Map<Integer, ItemStack> map = new ConcurrentHashMap<>();

                    for (final String key : memorySection.getKeys(false)) {
                        map.put(
                            Integer.parseInt(key),
                            Objects.requireNonNullElse(
                                memorySection.getItemStack(key),
                                new ItemStack(Material.AIR)
                            )
                        );
                    }

                    data = Cachable.of(map);
                }

                final PremadeKit kit = PremadeKit.kit(name)
                    .icon(icon)
                    .editable(editable)
                    .defaultLayout(data)
                    .displayName(displayName);

                kits.add(kit);
            }
        });
    }

    public static void stop() {
        for (final PremadeKit kit : kits) {
            kit.save();
        }
    }

    @NotNull
    public static Set<String> names() {
        final Set<String> names = new HashSet<>();

        for (final @NotNull PremadeKit kit : kits) {
            names.add(kit.name());
        }

        return Collections.unmodifiableSet(names);
    }

    @Nullable
    public static PremadeKit kit(final @NotNull String name) {
        for (final PremadeKit kit : kits) if (kit.name().equalsIgnoreCase(name)) return kit;
        return null;
    }

    @NotNull
    public static Set<PremadeKit> all() {
        return Collections.unmodifiableSet(kits);
    }

    public static void delete(final @NotNull PremadeKit kit) {
        kits.remove(kit);
    }

    public static void create(final @NotNull PremadeKit kit) {
        if (kits.contains(kit)) {
            kit.save();
            return;
        }

        kit.save();
        kits.add(kit);
    }
}
