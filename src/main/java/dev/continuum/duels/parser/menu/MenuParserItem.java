package dev.continuum.duels.parser.menu;

import dev.manere.utils.item.ItemBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuParserItem {
    private final ItemBuilder item;
    private final boolean enabled;
    private final List<Integer> slots;

    public MenuParserItem(final @NotNull ItemBuilder item, final @NotNull List<Integer> slots) {
        this.item = item;
        this.enabled = true;
        this.slots = slots;
    }


    public MenuParserItem(final @NotNull ItemBuilder item, final @NotNull List<Integer> slots, final boolean enabled) {
        this.item = item;
        this.enabled = enabled;
        this.slots = slots;
    }

    public static @NotNull MenuParserItem of(final @NotNull ItemBuilder item, final @NotNull List<Integer> slots) {
        return new MenuParserItem(item, slots);
    }

    public static @NotNull MenuParserItem of(final @NotNull ItemBuilder item, final @NotNull List<Integer> slots, final boolean enabled) {
        return new MenuParserItem(item, slots, enabled);
    }

    public ItemBuilder item() {
        return item;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<Integer> slots() {
        return slots;
    }
}
