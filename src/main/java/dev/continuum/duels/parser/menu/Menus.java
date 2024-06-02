package dev.continuum.duels.parser.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.util.Files;
import dev.manere.utils.menu.MenuBase;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Menus {
    @NotNull
    public static FileConfiguration load(final @NotNull String name) {
        Files.mkdirs(Files.file("menus/"));
        return Files.config(Files.create(Files.file("menus", name + ".yml")));
    }

    @CanIgnoreReturnValue
    @NotNull
    public static <T extends MenuBase<T>, M extends ParsedMenu<T>> M menu(final @NotNull Class<M> type, final @NotNull Object... parameters) {
        try {
            final Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(Object::getClass)
                .toArray(Class[]::new);

            return type.getDeclaredConstructor(parameterTypes)
                .newInstance(parameters);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
