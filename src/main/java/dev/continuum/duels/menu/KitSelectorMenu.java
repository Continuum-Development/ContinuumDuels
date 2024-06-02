package dev.continuum.duels.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.manere.utils.menu.normal.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitSelectorMenu extends ParsedMenu<Menu> {
    private final DuelMenu duelMenu;

    public KitSelectorMenu(final @NotNull DuelMenu duelMenu) {
        super(duelMenu.player(), Menu.class, "kit_selector_menu");
        this.duelMenu = duelMenu;
    }

    @Override
    public void init() {
        parser.placeItem(parser.normalItem("choose_from_custom_kits"), event -> {
            event.setCancelled(true);

            // TODO

            player.sendRichMessage("<dark_red><bold>NO IMPLEMENTATION FOR CUSTOM KITS");
            player.sendRichMessage("<dark_red>Changes need to be made to the entire database system");
            player.sendRichMessage("<dark_red>due to the oversight of not adding multiple custom-kit <bold>slot</bold> support");
        });

        parser.placeItem(parser.normalItem("choose_from_premade_kits"), event -> {
            event.setCancelled(true);

            PremadeKitSelectorMenu.open(duelMenu);
        });
    }

    @NotNull
    public DuelMenu duelMenu() {
        return duelMenu;
    }

    @NotNull
    @CanIgnoreReturnValue
    public static KitSelectorMenu open(final @NotNull DuelMenu duelMenu) {
        return new KitSelectorMenu(duelMenu).open();
    }

    @NotNull
    @CanIgnoreReturnValue
    public KitSelectorMenu open() {
        init();
        parser.menu().open(player);
        return this;
    }
}
