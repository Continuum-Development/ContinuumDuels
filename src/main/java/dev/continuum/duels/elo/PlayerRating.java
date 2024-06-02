package dev.continuum.duels.elo;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.config.ConfigHandler;
import dev.continuum.duels.config.ValueEditor;
import dev.continuum.duels.kit.Kit;
import dev.continuum.duels.kit.PremadeKits;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.PlayerSpecific;
import dev.continuum.duels.util.Savable;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.misc.ObjectUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DecimalFormat;
import java.util.UUID;

public class PlayerRating implements PlayerSpecific, Savable {
    private final UUID uuid;
    private final Cachable<Kit, Double> ratings = Cachable.of();

    public PlayerRating(final @NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public static PlayerRating rating(final @NotNull UUID uuid) {
        return new PlayerRating(uuid);
    }

    @NotNull
    @Override
    public UUID uuid() {
        return uuid;
    }

    public double rating(final @NotNull Kit kit) {
        return ObjectUtils.defaultIfNull(
            ratings.val(kit),
            ConfigHandler.value(
                Double.class,
                "ratings.starting_elo"
            )
        );
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerRating rating(final @NotNull Kit kit, final double rating) {
        this.ratings.cache(kit, rating);
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerRating edit(final @NotNull Kit kit, final @NotNull ValueEditor<@NotNull Double> editor) {
        return rating(kit, editor.edit(rating(kit)));
    }

    @NotNull
    public String ratingPretty(final @NotNull Kit kit) {
        return new DecimalFormat("#.#").format(rating(kit));
    }

    @Override
    public boolean save() {
        final File file = Files.create(
            new File(
                Files.mkdirs(Files.file("ratings/")),
                "<uuid>.yml"
                    .replaceAll(".yml", "")
                    .replaceAll(".yaml", "")
            )
        );

        final FileConfiguration configuration = Files.config(file);

        for (final Kit kit : ratings.snapshot().asMap().keySet()) {
            configuration.set("ratings." + kit.name(), rating(kit));
        }

        return Files.saveConfig(file, configuration);
    }

    @Override
    public boolean load() {
        final File file = Files.create(
            new File(
                Files.mkdirs(Files.file("ratings/")),
                "<uuid>.yml"
                    .replaceAll(".yml", "")
                    .replaceAll(".yaml", "")
            )
        );

        final FileConfiguration configuration = Files.config(file);

        for (final Kit kit : new PremadeKits().cached()) {
            final String name = kit.name();
            final String path = "ratings." + name;

            final double value = configuration.getDouble(path, ConfigHandler.value(
                Double.class,
                "ratings.starting_elo"
            ));

            this.ratings.cache(kit, value);
        }

        return true;
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }
}
