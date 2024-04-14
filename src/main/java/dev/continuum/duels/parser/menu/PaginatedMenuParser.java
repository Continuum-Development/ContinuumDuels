package dev.continuum.duels.parser.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.parser.item.ItemParser;
import dev.manere.utils.item.ItemBuilder;
import dev.manere.utils.menu.Button;
import dev.manere.utils.menu.Button.ButtonListener;
import dev.manere.utils.menu.paginated.PaginatedMenu;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.text.color.TextStyle;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaginatedMenuParser {
    private final FileConfiguration configuration;
    private PaginatedMenu menu;

    public PaginatedMenuParser(final @NotNull FileConfiguration configuration) {
        this.configuration = configuration;
    }

    public static @NotNull PaginatedMenuParser wrap(final @NotNull FileConfiguration configuration) {
        return new PaginatedMenuParser(configuration);
    }

    public int size() {
        final int size = configuration.getInt("size");
        if (size == 0) return 54;
        return size;
    }

    public @NotNull Component title() {
        final String raw = configuration.getString("title");
        if (raw == null) return Component.text("Failed to load title");
        return TextStyle.style(raw);
    }

    public @NotNull ConfigurationSection itemsSection() {
        final ConfigurationSection items = configuration.getConfigurationSection("items");
        if (items == null) return configuration.createSection("items");
        return items;
    }

    public @NotNull ConfigurationSection customItemsSection() {
        final ConfigurationSection items = configuration.getConfigurationSection("custom_items");
        if (items == null) return configuration.createSection("custom_items");
        return items;
    }

    public @NotNull MenuParserItem normalItem(final @NotNull String name) {
        final ConfigurationSection section = itemsSection().getConfigurationSection(name);
        if (section == null) return MenuParserItem.of(
                ItemBuilder.item(Material.AIR), List.of(10), false
        );

        final boolean enabled = section.getBoolean("enabled");
        final List<Integer> slots = section.getIntegerList("slots");

        final ItemBuilder builder = ItemParser.parse(
                configuration, "items." + name + ".item"
        );

        return MenuParserItem.of(builder, slots, enabled);
    }

    public @NotNull MenuParserItem customItem(final @NotNull String name) {
        final ConfigurationSection section = customItemsSection().getConfigurationSection(name);
        if (section == null) return MenuParserItem.of(
                ItemBuilder.item(Material.AIR), List.of(10), false
        );

        final List<Integer> slots = section.getIntegerList("slots");

        final ItemBuilder builder = ItemParser.parse(
                configuration, "custom_items." + name + ".item"
        );

        return MenuParserItem.of(builder, slots, true);
    }

    public void placeItem(final @NotNull MenuParserItem item, final @NotNull ButtonListener listener) {
        final List<Integer> slots = item.slots();
        final boolean enabled = item.enabled();
        final ItemBuilder builder = item.item();

        if (!enabled) return;

        for (final int slot : slots) {
            menu().stickyButton(slot, Button.button()
                    .item(builder)
                    .onClick(listener));
        }
    }

    public void placeItem(final @NotNull MenuParserItem item) {
        placeItem(item, event -> event.setCancelled(true));
    }

    @CanIgnoreReturnValue
    public PaginatedMenu menu() {
        if (this.menu == null) {
            this.menu = PaginatedMenu.menu(title(), size());
        }

        return this.menu;
    }

    public FileConfiguration configuration() {
        return configuration;
    }

    public @NotNull MenuParserItem normalItem(final @NotNull String name, final @NotNull List<Tuple<String, String>> replacements) {
        final ConfigurationSection section = itemsSection().getConfigurationSection(name);
        if (section == null) return MenuParserItem.of(
                ItemBuilder.item(Material.AIR), List.of(10), false
        );

        final boolean enabled = section.getBoolean("enabled");
        final List<Integer> slots = section.getIntegerList("slots");

        final ItemBuilder builder = ItemParser.parse(
                configuration,
                "items." + name + ".item",
                replacements
        );

        return MenuParserItem.of(builder, slots, enabled);
    }

    public void placeCustomItems() {
        final ConfigurationSection section = customItemsSection();

        for (final String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                placeItem(customItem(key));
            }
        }
    }
}
