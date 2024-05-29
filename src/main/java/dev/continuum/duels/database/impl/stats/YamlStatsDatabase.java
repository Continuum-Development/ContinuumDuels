package dev.continuum.duels.database.impl.stats;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.database.Database;
import dev.continuum.duels.stats.PlayerStats;
import dev.continuum.duels.util.Files;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlStatsDatabase implements Database<PlayerStats, UUID, PlayerStats> {
    @NotNull
    public File file(final @NotNull UUID uuid) {
        return Files.create(Files.file("statistics", uuid + ".yml"));
    }

    @NotNull
    public FileConfiguration configuration(final @NotNull UUID uuid) {
        return Files.config(file(uuid));
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
            Files.mkdirs(Files.file("statistics/"));
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
    public CompletableFuture<Boolean> save(final @NotNull PlayerStats value) {
        return CompletableFuture.supplyAsync(() -> {
            final File file = file(value.uuid());
            final FileConfiguration configuration = configuration(value.uuid());

            configuration.set("__player__", value.uuid().toString());
            configuration.set("kills", value.kills());
            configuration.set("deaths", value.deaths());
            configuration.set("unranked.wins", value.unrankedWins());
            configuration.set("unranked.losses", value.unrankedLosses());
            configuration.set("ranked.wins", value.rankedWins());
            configuration.set("ranked.losses", value.rankedLosses());

            return Files.saveConfig(file, configuration);
        });
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<PlayerStats> load(final @NotNull UUID value) {
        return CompletableFuture.supplyAsync(() -> {
            final FileConfiguration configuration = configuration(value);

            return PlayerStats.stats(value)
                .kills(configuration.getInt("kills", 0))
                .deaths(configuration.getInt("deaths", 0))
                .unrankedWins(configuration.getInt("unranked.wins", 0))
                .unrankedLosses(configuration.getInt("unranked.losses", 0))
                .rankedWins(configuration.getInt("ranked.wins", 0))
                .rankedLosses(configuration.getInt("ranked.losses", 0));
        });
    }
}
