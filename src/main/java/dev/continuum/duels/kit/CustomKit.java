package dev.continuum.duels.kit;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.database.provider.DatabaseProviders;
import dev.continuum.duels.util.PlayerSpecific;
import dev.manere.utils.elements.Elements;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CustomKit implements Kit, PlayerSpecific {
    private KitContents contents = KitContents.empty();
    private Arena arena = Arenas.any();

    private final String name;
    private final UUID owner;

    private String displayName = null;

    public CustomKit(final @NotNull String name, final @NotNull UUID owner) {
        this.name = name;
        this.owner = owner;
    }

    @NotNull
    @Override
    public String displayName() {
        if (displayName != null) return displayName;

        return Kit.super.displayName();
    }

    @NotNull
    @Override
    public Kit displayName(final @NotNull String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public boolean premade() {
        return false;
    }

    @Override
    public boolean custom() {
        return true;
    }

    @NotNull
    @Override
    public KitContents contents() {
        return contents;
    }

    @NotNull
    @Override
    public String name() {
        return name;
    }

    @NotNull
    @Override
    public Material icon() {
        return Material.AMETHYST_SHARD;
    }

    /**
     * @return a list of only one element (a single arena)
     */
    @NotNull
    @Override
    public Elements<Arena> arenas() {
        return Elements.of(arena);
    }

    @NotNull
    @Override
    public Kit contents(final @NotNull KitContents contents) {
        this.contents = contents;
        return this;
    }

    @NotNull
    @Override
    public Kit icon(final @NotNull Material icon) {
        throw new UnsupportedOperationException("A CustomKit can only have one icon: Amethyst Shard");
    }

    @NotNull
    @Override
    public Kit arenas(final @NotNull Elements<Arena> arenas) {
        this.arena = arenas.element(0);
        return this;
    }

    @NotNull
    @Override
    public Kit addArenas(final @NotNull Arena @NotNull ... arenas) {
        this.arena = arenas[0];
        return this;
    }

    @NotNull
    @Override
    public Kit addArenas(final @NotNull Elements<Arena> arenas) {
        this.arena = arenas.element(0);
        return this;
    }

    @Override
    public boolean save() {
        try {
            return DatabaseProviders.kits()
                .database()
                .save(this)
                .get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean load() {
        try {
            return DatabaseProviders.kits()
                .database()
                .load(this)
                .get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @NotNull
    @Override
    public CompletableFuture<Boolean> saveAsync() {
        return DatabaseProviders.kits()
            .database()
            .save(this);
    }

    @NotNull
    @Override
    public CompletableFuture<Boolean> loadAsync() {
        return DatabaseProviders.kits()
            .database()
            .load(this);
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public UUID uuid() {
        return owner;
    }
}
