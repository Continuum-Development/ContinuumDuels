package dev.continuum.duels.database.provider;

import dev.continuum.duels.database.DatabaseProvider;
import dev.continuum.duels.kit.CustomKit;
import dev.continuum.duels.stats.PlayerStats;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DatabaseProviders {
    @NotNull
    public static DatabaseProvider<CustomKit, CustomKit, Boolean> kits() {
        return new KitDatabaseProvider();
    }

    @NotNull
    public static DatabaseProvider<PlayerStats, UUID, PlayerStats> statistics() {
        return new StatsDatabaseProvider();
    }
}
