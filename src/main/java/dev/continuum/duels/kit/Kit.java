package dev.continuum.duels.kit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.util.Savable;
import dev.manere.utils.elements.Elements;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public interface Kit extends Savable {
    boolean premade();

    boolean custom();

    @NotNull
    KitContents contents();

    @NotNull
    String name();

    @NotNull
    Material icon();

    @NotNull
    Elements<Arena> arenas();

    @NotNull
    @CanIgnoreReturnValue
    Kit contents(final @NotNull KitContents contents);

    @NotNull
    @CanIgnoreReturnValue
    Kit icon(final @NotNull Material icon);

    @NotNull
    @CanIgnoreReturnValue
    Kit displayName(final @NotNull String displayName);

    @NotNull
    @CanIgnoreReturnValue
    Kit arenas(final @NotNull Elements<Arena> arenas);

    @NotNull
    @CanIgnoreReturnValue
    Kit addArenas(final @NotNull Arena @NotNull ... arenas);

    @NotNull
    @CanIgnoreReturnValue
    Kit addArenas(final @NotNull Elements<Arena> arenas);

    @NotNull
    default String displayName() {
        return StringUtils.capitalize(name());
    }
}
