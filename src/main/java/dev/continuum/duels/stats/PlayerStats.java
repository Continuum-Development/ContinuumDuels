package dev.continuum.duels.stats;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.database.provider.DatabaseProviders;
import dev.continuum.duels.util.PlayerSpecific;
import dev.continuum.duels.util.Savable;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerStats implements PlayerSpecific, Savable {
    private final UUID uuid;

    private int kills = 0;
    private int deaths = 0;
    private int unrankedWins = 0;
    private int unrankedLosses = 0;
    private int rankedWins = 0;
    private int rankedLosses = 0;

    public PlayerStats(final @NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public static PlayerStats stats(final @NotNull UUID uuid) {
        return new PlayerStats(uuid);
    }

    public int kills() {
        return kills;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats kills(int kills) {
        this.kills = kills;
        return this;
    }

    public int deaths() {
        return deaths;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats deaths(final int deaths) {
        this.deaths = deaths;
        return this;
    }

    public int unrankedWins() {
        return unrankedWins;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats unrankedWins(final int unrankedWins) {
        this.unrankedWins = unrankedWins;
        return this;
    }

    public int unrankedLosses() {
        return unrankedLosses;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats unrankedLosses(final int unrankedLosses) {
        this.unrankedLosses = unrankedLosses;
        return this;
    }

    public int rankedWins() {
        return rankedWins;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats rankedWins(final int rankedWins) {
        this.rankedWins = rankedWins;
        return this;
    }

    public int rankedLosses() {
        return rankedLosses;
    }

    @NotNull
    @CanIgnoreReturnValue
    public PlayerStats rankedLosses(final int rankedLosses) {
        this.rankedLosses = rankedLosses;
        return this;
    }

    public int totalWins() {
        return rankedWins + unrankedWins;
    }

    public int totalLosses() {
        return rankedLosses + unrankedLosses;
    }

    @NotNull
    public String kdr() {
        if (kills == 0 && deaths == 0) return "0.00";
        final double kdr = (double) kills / deaths;
        return new DecimalFormat("#.##").format(kdr);
    }

    @NotNull
    public String unrankedWlr() {
        if (unrankedWins == 0 && unrankedLosses == 0) return "0.00";
        final double wlr = (double) unrankedWins / unrankedLosses;
        return new DecimalFormat("#.##").format(wlr);
    }

    @NotNull
    public String rankedWlr() {
        if (rankedWins == 0 && rankedLosses == 0) return "0.00";
        final double wlr = (double) rankedWins / rankedLosses;
        return new DecimalFormat("#.##").format(wlr);
    }

    @NotNull
    public String totalWlr() {
        if (totalWins() == 0 && totalLosses() == 0) return "0.00";
        final double wlr = (double) totalWins() / totalLosses();
        return new DecimalFormat("#.##").format(wlr);
    }

    @NotNull
    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public boolean save() {
        try {
            return saveAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> saveAsync() {
        return DatabaseProviders.statistics()
            .database()
            .save(this);
    }

    @Override
    public boolean load() {
        try {
            return loadAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public @NotNull CompletableFuture<Boolean> loadAsync() {
        return CompletableFuture.supplyAsync(() -> {
            DatabaseProviders.statistics()
                .database()
                .load(uuid)
                .thenAccept((stats) -> {
                    this.kills = stats.kills;
                    this.deaths = stats.deaths;
                    this.unrankedLosses = stats.unrankedLosses;
                    this.unrankedWins = stats.unrankedWins;
                    this.rankedLosses = stats.rankedLosses;
                    this.rankedWins = stats.rankedWins;
                });

            return true;
        });
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }
}
