package dev.continuum.duels.arena;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.SavableCache;
import dev.manere.utils.elements.Elements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class PremadeArenas implements SavableCache<PremadeArena> {
    private static final Elements<PremadeArena> cached = Elements.of();

    @Nullable
    public static PremadeArena arena(final @NotNull String name) {
        for (final PremadeArena arena : cached) if (arena.name().equals(name)) return arena;
        return null;
    }

    @Nullable
    public static PremadeArena any() {
        if (cached.size() == 0) return null;

        final int index = ThreadLocalRandom.current().nextInt(0, cached.size() + 1);
        return cached.element(index);
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Void> startAndLoad() {
        return CompletableFuture.runAsync(() -> {
            final File file = Files.mkdirs(
                Files.file(
                    "arenas/"
                )
            );

            final File[] children = file.listFiles();
            if (children == null) return;

            for (final File child : children) {
                final String name = child.getName().replaceAll(".yml", "");

                final PremadeArena arena = PremadeArena.arena(name);
                arena.load(true);

                cached.element(arena);
            }
        });
    }

    @Override
    public void stopAndSave() {
        for (final PremadeArena arena : cached()) {
            arena.save(false);
        }
    }

    @NotNull
    @Override
    public Elements<PremadeArena> cached() {
        return cached;
    }
}
