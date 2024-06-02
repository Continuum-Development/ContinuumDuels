package dev.continuum.duels.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.kit.PremadeKits;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.library.Utils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface SavableCache<S extends Savable> {
    @NotNull
    @CanIgnoreReturnValue
    static <S extends Savable, C extends SavableCache<S>> C cache(final @NotNull CacheAction action, final @NotNull Class<C> type, final @NotNull String name) {
        try {
            final C cache = type.getDeclaredConstructor().newInstance();

            if (action == CacheAction.START) {
                final Instant startTime = Instant.now();

                cache.startAndLoad()
                    .thenRun(() -> Utils.plugin().getLogger().log(Level.INFO, "Loading ${name}..."
                        .replaceAll("\\$\\{name}", name)
                    ))
                    .whenComplete(($, $$) -> {
                        final Instant endTime = Instant.now();
                        final long timeElapsed = Duration.between(
                            startTime,
                            endTime
                        ).toMillis();

                        Utils.plugin().getLogger().log(Level.INFO, "Loaded ${name} in ${time}"
                            .replaceAll("\\$\\{name}", name)
                            .replaceAll("\\$\\{time}", DurationFormatUtils.formatDurationHMS(timeElapsed))
                        );
                    });
            } else if (action == CacheAction.STOP) {
                final Instant startTime = Instant.now();

                Utils.plugin().getLogger().log(Level.INFO, "Stopping ${name}..."
                    .replaceAll("\\$\\{name}", name)
                );

                cache.stopAndSave();

                final Instant endTime = Instant.now();
                final long timeElapsed = Duration.between(
                    startTime,
                    endTime
                ).toMillis();

                Utils.plugin().getLogger().log(Level.INFO, "Stopped ${name} in ${time}"
                    .replaceAll("\\$\\{name}", name)
                    .replaceAll("\\$\\{time}", DurationFormatUtils.formatDurationHMS(timeElapsed))
                );
            }

            return cache;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    static <S extends Savable, C extends SavableCache<S>> C cache(final @NotNull Class<C> type) {
        return cache(CacheAction.NONE, type, "");
    }

    @NotNull
    @CanIgnoreReturnValue
    CompletableFuture<Void> startAndLoad();

    void stopAndSave();

    @NotNull
    Elements<S> cached();
}
