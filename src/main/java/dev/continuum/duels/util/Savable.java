package dev.continuum.duels.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.model.Tuple;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface Savable {
    boolean save();

    @NotNull
    default CompletableFuture<Boolean> saveAsync() {
        return CompletableFuture.supplyAsync(this::save);
    }

    boolean load();

    @NotNull
    default CompletableFuture<Boolean> loadAsync() {
        return CompletableFuture.supplyAsync(this::load);
    }

    @CanIgnoreReturnValue
    default boolean save(final boolean async) {
        try {
            return async ? saveAsync().get() : save();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @CanIgnoreReturnValue
    default boolean load(final boolean async) {
        try {
            return async ? loadAsync().get() : load();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void delete();

    @NotNull
    default CompletableFuture<Void> deleteAsync() {
        return CompletableFuture.runAsync(this::delete);
    }

    @CanIgnoreReturnValue
    default void delete(final boolean async) {
        if (async) {
            deleteAsync();
        } else {
            delete();
        }
    }
}
