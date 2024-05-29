package dev.continuum.duels.database;

import dev.continuum.duels.database.Database;
import org.jetbrains.annotations.NotNull;

public interface DatabaseProvider<S, LV, LR> {
    @NotNull
    Database<S, LV, LR> database();

    void start();

    void stop();
}
