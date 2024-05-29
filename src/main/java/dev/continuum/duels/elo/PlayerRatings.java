package dev.continuum.duels.elo;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.PremadeArena;
import dev.continuum.duels.config.ValueEditor;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.SavableCache;
import dev.manere.utils.elements.Elements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRatings implements SavableCache<PlayerRating> {
    private static final Elements<PlayerRating> cached = Elements.of();

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Void> startAndLoad() {
        return CompletableFuture.runAsync(() -> {
            final File file = Files.mkdirs(
                Files.file(
                    "ratings/"
                )
            );

            final File[] children = file.listFiles();
            if (children == null) return;

            for (final File child : children) {
                final String uuid = child.getName().replaceAll(".yml", "");

                final PlayerRating rating = PlayerRating.rating(UUID.fromString(uuid));
                rating.load();

                cached.element(rating);
            }
        });
    }

    @Override
    public void stopAndSave() {
        for (final PlayerRating rating : cached()) {
            rating.save(false);
        }
    }

    @NotNull
    @Override
    public Elements<PlayerRating> cached() {
        return cached;
    }

    @Nullable
    public PlayerRating rating(final @NotNull UUID uuid) {
        for (final PlayerRating rating : cached()) if (rating.uuid().equals(uuid)) return rating;
        return null;
    }

    @Nullable
    public PlayerRating ratingOrCreate(final @NotNull UUID uuid) {
        for (final PlayerRating rating : cached()) if (rating.uuid().equals(uuid)) return rating;

        final PlayerRating defaultRating = PlayerRating.rating(uuid);
        defaultRating.load();

        if (!cached.has(defaultRating)) cached.element(defaultRating);
        return defaultRating;
    }
}
