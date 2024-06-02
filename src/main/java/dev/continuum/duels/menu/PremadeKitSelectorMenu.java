package dev.continuum.duels.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.kit.PremadeKit;
import dev.continuum.duels.kit.PremadeKits;
import dev.continuum.duels.parser.menu.PaginatedMenuParser;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.continuum.duels.util.SavableCache;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.item.ItemBuilder;
import dev.manere.utils.menu.Button;
import dev.manere.utils.menu.normal.Menu;
import dev.manere.utils.menu.paginated.PaginatedMenu;
import dev.manere.utils.menu.paginated.PaginatedSlot;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.text.color.TextStyle;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.awt.desktop.AppReopenedEvent;
import java.util.ArrayList;
import java.util.List;

public class PremadeKitSelectorMenu extends ParsedMenu<PaginatedMenu> {
    private final DuelMenu duelMenu;

    public PremadeKitSelectorMenu(final @NotNull DuelMenu duelMenu) {
        super(duelMenu.player(), PaginatedMenu.class, "premade_kit_selector_menu");
        this.duelMenu = duelMenu;
    }

    @Override
    public void init() {
        final PremadeKits cache = SavableCache.cache(PremadeKits.class);
        final Elements<PremadeKit> kits = cache.cached();
        final FileConfiguration configuration = parser().configuration();

        int start = configuration.getInt("kit_button.start");
        if (start == 0 || start == -1) start = 19;

        int end = configuration.getInt("kit_button.end");
        if (end == 0 || end == -1) end = 44;

        int slot = start;
        int page = 1;

        if (parser().menu().currentPage() > 1) {
            parser().placeItem(parser().normalItem("previous_page_button"), event -> {
                event.setCancelled(true);

                if (parser().menu().currentPage() > 0) {
                    parser().menu().previousPage(player);
                }
            });
        }

        if (parser().menu().currentPage() != 1) {
            parser().placeItem(parser().normalItem("current_page_button",
                List.of(
                    Tuple.tuple(
                        "page",
                        String.valueOf(parser().menu().currentPage())
                    ),
                    Tuple.tuple(
                        "current page",
                        String.valueOf(parser().menu().currentPage())
                    ),
                    Tuple.tuple(
                        "current",
                        String.valueOf(parser().menu().currentPage())
                    )
                ))
            );
        }

        if (parser().menu().currentPage() < parser().menu().totalPages()) {
            parser().placeItem(parser().normalItem("next_page_button"), event -> {
                event.setCancelled(true);

                if (parser().menu().currentPage() < parser().menu().totalPages()) {
                    parser().menu().nextPage(player);
                }
            });
        }

        for (final PremadeKit kit : kits) {
            if (slot == end) {
                page++;
                slot = 19;
            }

            if (slot == 8 || slot == 9) slot = 10;
            if (slot == 17 || slot == 18) slot = 19;
            if (slot == 26 || slot == 27) slot = 28;
            if (slot == 35 || slot == 36) slot = 37;
            if (slot == 44 || slot == 45) slot = 46;

            final Material icon = kit.icon();

            String rawName = configuration.getString("kit_button.item.name");
            if (rawName == null) rawName = "<display name>";

            rawName = rawName.replaceAll("<display name>", kit.displayName());
            rawName = rawName.replaceAll("<display>", kit.displayName());
            rawName = rawName.replaceAll("<name>", kit.name());

            final Component name = TextStyle.style(rawName);

            final List<String> rawLore = configuration.getStringList("kit_button.item.lore");
            final List<String> formattedRawLore = new ArrayList<>();

            for (String line : rawLore) {
                line = line.replaceAll("<name>", kit.name());
                line = line.replaceAll("<display name>", kit.displayName());
                line = line.replaceAll("<display>", kit.displayName());

                formattedRawLore.add(line);
            }

            final List<Component> lore = TextStyle.style(formattedRawLore);

            ItemBuilder item = ItemBuilder.item(icon)
                .name(name)
                .lore(lore)
                .hideAttributes();

            for (final ItemFlag flag : ItemFlag.values()) {
                item.addFlag(flag);
            }

            parser.menu().button(PaginatedSlot.paginatedSlot(slot, page), Button.button(item).onClick(event -> {
                event.setCancelled(true);

                duelMenu.kit(kit);

                duelMenu.open();
            }));
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    public static PremadeKitSelectorMenu open(final @NotNull DuelMenu duelMenu) {
        return new PremadeKitSelectorMenu(duelMenu).open();
    }

    @NotNull
    @CanIgnoreReturnValue
    public PremadeKitSelectorMenu open() {
        init();
        parser.menu().open(player);
        return this;
    }
}
