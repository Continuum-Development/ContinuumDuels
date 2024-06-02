package dev.continuum.duels.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.parser.item.ItemParser;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.item.ItemBuilder;
import dev.manere.utils.menu.Button;
import dev.manere.utils.menu.normal.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.manere.utils.model.Tuple.tuple;

public class RoundSelectorMenu extends ParsedMenu<Menu> {
    private final DuelMenu previousMenu;

    private static final Cachable<Integer, String> iDoNotKnowWhatToCallThis = Cachable.of(
        tuple(1, "one"),
        tuple(2, "two"),
        tuple(3, "three"),
        tuple(4, "four"),
        tuple(5, "five"),
        tuple(10, "ten"),
        tuple(20, "twenty"),
        tuple(25, "twenty_five"),
        tuple(50, "fifty"),
        tuple(100, "one_hundred")
    );

    public RoundSelectorMenu(final @NotNull DuelMenu previousMenu) {
        super(previousMenu.player(), Menu.class, "round_selector_menu");
        this.previousMenu = previousMenu;
    }

    @Override
    public void init() {
        final Elements<Integer> roundNumbers = Elements.of(
            1, 2, 3, 4, 5,
            10, 20,
            25, 50,
            100
        );

        for (final int roundNumber : roundNumbers) {
            final String valueOf = String.valueOf(roundNumber);

            final ItemBuilder item = ItemParser.parse(parser.configuration(), "round_item.item", List.of(
                tuple(
                    "rounds",
                    valueOf
                )
            ));

            final int slot = parser.configuration().getInt("round_item.slots." + iDoNotKnowWhatToCallThis.val(roundNumber));

            parser.menu().button(slot, Button.button(item).onClick(event -> {
                event.setCancelled(true);

                previousMenu.rounds(roundNumber);
                previousMenu.open();
            }));
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    public RoundSelectorMenu open() {
        init();
        parser.menu().open(player);
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public static RoundSelectorMenu open(final @NotNull DuelMenu previousMenu) {
        return new RoundSelectorMenu(previousMenu).open();
    }

    @NotNull
    public DuelMenu previousMenu() {
        return previousMenu;
    }
}
