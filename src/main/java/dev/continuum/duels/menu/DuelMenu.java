package dev.continuum.duels.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.PremadeArena;
import dev.continuum.duels.arena.PremadeArenas;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.duel.DuelRequest;
import dev.continuum.duels.kit.Kit;
import dev.continuum.duels.parser.menu.ParsedMenu;
import dev.manere.utils.menu.normal.Menu;
import dev.manere.utils.model.Tuple;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class DuelMenu extends ParsedMenu<Menu> {
    private final Player target;
    private int rounds = 1;
    private Kit kit;
    private PremadeArena arena;

    public DuelMenu(final @NotNull Player player, final @NotNull Player target) {
        super(player, Menu.class, "duel_menu");
        this.target = target;
    }

    @Override
    public void init() {
        parser().placeItem(parser.normalItem("duel_target", List.of(
            Tuple.tuple(
                "target",
                target.getName()
            )
        )), event -> event.setCancelled(true));

        parser().placeItem(parser.normalItem("rounds", List.of(
            Tuple.tuple(
                "rounds",
                String.valueOf(rounds)
            )
        )), event -> {
            event.setCancelled(true);
            RoundSelectorMenu.open(this);
        });

        parser.placeItem(parser.normalItem("kit", List.of(
            Tuple.tuple(
                "kit",
                kit != null ? kit.displayName() : parser.placeholder("kit_not_selected")
            )
        )), event -> {
            event.setCancelled(true);
            KitSelectorMenu.open(this);
        });

        if (kit != null && arena == null) this.arena = PremadeArenas.any(kit);

        parser.placeItem(parser.normalItem("arena", List.of(
            Tuple.tuple(
                "arena",
                arena != null ? arena.info().displayName() : parser.placeholder("arena_not_selected")
            )
        )), event -> {
            event.setCancelled(true);
            ArenaSelectorMenu.open(this);
        });

        parser.placeItem(parser.normalItem("confirm_and_send"), event -> {
            if (kit == null) {
                Messages.message("kit_not_set_duel_menu", player);
                return;
            }

            if (arena == null) {
                Messages.message("arena_not_set_duel_menu", player);
                return;
            }

            for (final DuelRequest duelRequest : DuelRequest.requests()) {
                if (
                    duelRequest.sender().getUniqueId().equals(player.getUniqueId())
                        &&
                        duelRequest.target().getUniqueId().equals(target.getUniqueId())
                ) {
                    Messages.message("already_sent_duel_to_that_player", player, replacements -> {
                        replacements.element(Tuple.tuple("target", target.getName()));
                        return replacements;
                    });

                    return;
                }
            }

            DuelRequest.request(player, target, rounds, kit, arena);
        });
    }

    @NotNull
    @CanIgnoreReturnValue
    public static DuelMenu open(final @NotNull Player sender, final @NotNull Player target) {
        return new DuelMenu(sender, target).open();
    }

    @NotNull
    public Player target() {
        return target;
    }

    public int rounds() {
        if (rounds == -1) return 0;
        return rounds;
    }

    @NotNull
    @CanIgnoreReturnValue
    public DuelMenu rounds(final int rounds) {
        this.rounds = rounds;
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public DuelMenu open() {
        init();
        parser.menu().open(player);
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public DuelMenu kit(final @NotNull Kit kit) {
        this.kit = kit;
        return this;
    }

    @NotNull
    @CanIgnoreReturnValue
    public DuelMenu arena(final @NotNull PremadeArena arena) {
        this.arena = arena;
        return this;
    }

    @Nullable
    public Arena arena() {
        return arena;
    }
}
