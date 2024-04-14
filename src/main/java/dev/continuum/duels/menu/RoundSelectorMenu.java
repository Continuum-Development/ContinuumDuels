package dev.continuum.duels.menu;

import dev.continuum.duels.parser.item.ItemParser;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.item.ItemBuilder;
import dev.manere.utils.model.Tuple;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RoundSelectorMenu extends ParsedMenu {
    private final DuelMenu duelMenu;
    private static final Cachable<Integer, String> iDoNotKnowWhatToCallThis = Cachable.of(
        Tuple.tuple(
            1, "one"
        ),
        Tuple.tuple(
            2, "two"
        ),
        Tuple.tuple(
            3, "three"
        ),
        Tuple.tuple(
            4, "four"
        ),
        Tuple.tuple(
            5, "five"
        ),
        Tuple.tuple(
            10, "ten"
        ),
        Tuple.tuple(
            20, "twenty"
        ),
        Tuple.tuple(
            25, "twenty_five"
        ),
        Tuple.tuple(
            50, "fifty"
        ),
        Tuple.tuple(
            100, "one_hundred"
        )
    );

    public RoundSelectorMenu(final @NotNull Player player, final @NotNull DuelMenu duelMenu) {
        super(player, "round_selector_menu");
        this.duelMenu = duelMenu;
    }

    @Override
    public void init() {
        // 1, 2, 3, 4, 5, 10, 15, 20, 25, 50, 100
        final Elements<Integer> roundNumbers = Elements.of(
            1, 2, 3, 4, 5,
            10, 20,
            25, 50,
            100
        );

        for (final int roundNumber : roundNumbers) {
            final String valueOf = String.valueOf(roundNumber);

            final ItemBuilder stack = ItemParser.parse(parser.configuration(), "round_item.item", List.of(
                Tuple.tuple(
                    "rounds",
                    valueOf
                )
            ));
        }
    }
}
