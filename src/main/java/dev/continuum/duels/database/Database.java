package dev.continuum.duels.database;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface Database<S, LV, LR> {
    @NotNull
    Elements<String> types();

    @NotNull
    Cachable<String, Class<?>> requiredDetails();

    @NotNull
    @CanIgnoreReturnValue
    CompletableFuture<Boolean> start(final @NotNull Cachable<String, Object> details);

    @CanIgnoreReturnValue
    boolean stop();

    @NotNull
    @CanIgnoreReturnValue
    CompletableFuture<Boolean> save(final @NotNull S value);

    @NotNull
    @CanIgnoreReturnValue
    CompletableFuture<LR> load(final @NotNull LV value);
}
