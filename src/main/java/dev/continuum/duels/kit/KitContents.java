package dev.continuum.duels.kit;

import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.misc.ObjectUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public interface KitContents {
    @NotNull
    static KitContents empty() {
        return () -> Cachable.of(Integer.class, ItemStack.class);
    }

    @NotNull
    static KitContents create(final @NotNull Cachable<Integer, ItemStack> contents) {
        return () -> contents;
    }

    @NotNull
    static KitContents create(final @NotNull Supplier<Cachable<Integer, ItemStack>> contents) {
        return contents::get;
    }

    @NotNull
    Cachable<Integer, ItemStack> contents();

    default void give(final @NotNull Player player) {
        player.getOpenInventory().getBottomInventory().clear();

        for (final Map.Entry<Integer, ItemStack> entry : contents().snapshot().asMap().entrySet()) {
            final int index = entry.getKey();
            final ItemStack item = ObjectUtils.defaultIfNull(entry.getValue(), new ItemStack(Material.AIR));

            player.getOpenInventory().getBottomInventory().setItem(index, item);
        }
    }
}
