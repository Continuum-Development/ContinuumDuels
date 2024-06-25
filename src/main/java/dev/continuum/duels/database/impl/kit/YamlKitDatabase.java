package dev.continuum.duels.database.impl.kit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.PremadeArenas;
import dev.continuum.duels.database.Database;
import dev.continuum.duels.kit.CustomKit;
import dev.continuum.duels.kit.KitContents;
import dev.continuum.duels.util.Files;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.model.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class YamlKitDatabase implements Database<CustomKit, Tuple<UUID, Integer>, CustomKit> {
    @NotNull
    public File file(final int kit, UUID uuid) {
        Files.mkdirs(Files.file("custom_kits_" + kit + "/"));
        return Files.create(Files.file("custom_kits_" + kit, uuid + ".yml"));
    }

    @NotNull
    public FileConfiguration configuration(final int kit, final @NotNull UUID uuid) {
        return Files.config(file(kit, uuid));
    }

    @NotNull
    @Override
    public Elements<String> types() {
        return Elements.of(
            "yaml", "yml",
            "files", "disk",
            "default"
        );
    }

    @NotNull
    @Override
    public Cachable<String, Class<?>> requiredDetails() {
        return Cachable.of();
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Boolean> start(final @NotNull Cachable<String, Object> details) {
        return CompletableFuture.supplyAsync(() -> {
            Files.mkdirs(Files.file("custom_kits/"));
            return true;
        });
    }

    @Override
    @CanIgnoreReturnValue
    public boolean stop() {
        return true;
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Boolean> save(final @NotNull CustomKit value) {
        return CompletableFuture.supplyAsync(() -> {
            final File file = file(value.kit(), value.uuid());
            final Arena arena = value.arenas().element(0);
            final FileConfiguration configuration = configuration(value.kit(), value.uuid());

            configuration.set("__owner__", value.uuid().toString());
            configuration.set("__name__", value.name());

            configuration.set("display_name", value.displayName());

            configuration.set("settings.arena", arena == null ? null : arena.name());
            configuration.set("settings.contents", value.contents().contents().snapshot().asMap());

            return Files.saveConfig(file, configuration);
        });
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    @SuppressWarnings("DataFlowIssue")
    public CompletableFuture<CustomKit> load(final @NotNull Tuple<UUID, Integer> value) {
        return CompletableFuture.supplyAsync(() -> {
            final FileConfiguration configuration = configuration(value.val(), value.key());

            final CustomKit kit = new CustomKit(value.val(), configuration.getString("__name__", ""), UUID.fromString(configuration.getString("__owner__")));

            final Elements<Arena> arenas = Elements.of(Arena.class);
            arenas.element(0, PremadeArenas.arena(configuration.getString("settings.arena", "failed_to_find")));
            kit.arenas(arenas);

            kit.displayName(configuration.getString("display_name", StringUtils.capitalize(kit.name())));

            final ConfigurationSection section = configuration.getConfigurationSection("settings");
            if (section == null) {
                kit.contents(KitContents.empty());
                return kit;
            }

            final Object raw = section.get("contents");
            if (raw == null) {
                kit.contents(KitContents.empty());
                return kit;
            }

            if (raw instanceof Map<?, ?> map) {
                //noinspection unchecked
                kit.contents(KitContents.create(Cachable.of((Map<Integer, ItemStack>) map)));
            } else if (raw instanceof MemorySection memorySection) {
                final Map<Integer, ItemStack> data = new ConcurrentHashMap<>();

                for (final String key : memorySection.getKeys(false)) {
                    data.put(
                        Integer.parseInt(key),
                        Objects.requireNonNullElse(
                            memorySection.getItemStack(key),
                            new ItemStack(Material.AIR)
                        )
                    );
                }

                kit.contents(KitContents.create(Cachable.of(data)));
            } else {
                kit.contents(KitContents.empty());
            }

            return kit;
        });
    }
}
