package dev.continuum.duels.menu;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.kit.premade.PremadeKit;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuelMenu extends ParsedMenu {
    private final Player target;
    private Object kit;
    private Arena arena;
    private boolean isPremadeKit;
    private int rounds = 1;

    public DuelMenu(final @NotNull Player player, final @NotNull Player target, final int rounds) {
        super(player, "duel_menu");
        this.target = target;
        this.rounds = rounds;

        if (!(kit instanceof PremadeKit)) {
            isPremadeKit = false;
        }
    }

    @Override
    public void init() {
        if (kit instanceof PremadeKit premadeKit) {
            parser.placeItem(parser.normalItem("arena", List.of(
                Tuple.tuple(
                    "arena",
                    ObjectUtils.defaultIfNull(arena.name(), parser.placeholder("not_selected"))
                )
            )), event -> {
                event.setCancelled(true);
                // TODO: Menus.menu(ArenaSelectorMenu.class, this)
            });

            parser.placeItem(parser.normalItem("kit", List.of(
                Tuple.tuple(
                    "kit",
                    ObjectUtils.defaultIfNull(premadeKit.name(), parser.placeholder("not_selected"))
                )
            )), event -> {
                event.setCancelled(true);
                // TODO: Menus.menu(PremadeKitSelectorMenu.class, this)
            });

            parser.placeItem(parser.normalItem("rounds", List.of(
                Tuple.tuple(
                    "rounds",
                    String.valueOf(rounds)
                )
            )), event -> {
                event.setCancelled(true);
                // TODO: Menus.menu(RoundSelectorMenu.class, this)
            });

            parser.placeItem(parser.normalItem("confirm", List.of(
                Tuple.tuple(
                    "arena",
                    ObjectUtils.defaultIfNull(arena.name(), parser.placeholder("not_selected"))
                ),
                Tuple.tuple(
                    "kit",
                    ObjectUtils.defaultIfNull(premadeKit.name(), parser.placeholder("not_selected"))
                ),
                Tuple.tuple(
                    "rounds",
                    String.valueOf(rounds)
                )
            )), event -> {
                event.setCancelled(true);
                // TODO: send
            });

            parser.placeItem(parser.normalItem("cancel"), event -> {
                event.setCancelled(true);
                player.closeInventory();
            });
        }
    }
}
