package dev.continuum.duels.kit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.SavableCache;
import dev.manere.utils.elements.Elements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class PremadeKits implements SavableCache<PremadeKit> {
    private static final Elements<PremadeKit> cached = Elements.of();

    @Nullable
    public static PremadeKit kit(final @NotNull String name) {
        for (final PremadeKit kit : cached) if (kit.name().equals(name)) return kit;
        return null;
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Void> startAndLoad() {
        return CompletableFuture.runAsync(() -> {
            final File file = Files.mkdirs(
                Files.file(
                    "premade_kits/"
                )
            );

            final File[] children = file.listFiles();
            if (children == null) return;

            for (final File child : children) {
                final String name = child.getName().replaceAll(".yml", "");

                final PremadeKit kit = PremadeKit.kit(name);
                kit.load(true);

                cached.element(kit);
            }
        });
    }

    @Override
    public void stopAndSave() {
        for (final PremadeKit kit : cached()) {
            kit.save(false);
        }
    }

    @NotNull
    @Override
    public Elements<PremadeKit> cached() {
        return cached;
    }
}
