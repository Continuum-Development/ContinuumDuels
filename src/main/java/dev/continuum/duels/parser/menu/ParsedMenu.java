package dev.continuum.duels.parser.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.manere.utils.menu.MenuBase;
import dev.manere.utils.registration.Registrar;
import dev.manere.utils.scheduler.Schedulers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ParsedMenu<M extends MenuBase<M>> implements Listener {
    protected final Player player;
    protected final MenuParser<M> parser;

    public ParsedMenu(final @NotNull Player player, final @NotNull Class<M> menuType, final @NotNull String fileName) {
        this.player = player;
        this.parser = MenuParser.wrap(Menus.load(fileName), menuType);
        this.parser.menu();

        Registrar.events(this);
    }

    public abstract void init();

    @EventHandler
    public final void execute(final @NotNull InventoryCloseEvent event) {
        if (!event.getInventory().equals(parser.menu().inventory())) return;

        onClose(event);

        Schedulers.async().execute(() -> HandlerList.unregisterAll(this), 10);
    }

    public void onClose(final @NotNull InventoryCloseEvent event) {}

    @EventHandler
    public final void execute(final @NotNull InventoryClickEvent event) {
        if (!event.getInventory().equals(parser.menu().inventory())) return;

        onClick(event);

        Schedulers.async().execute(() -> HandlerList.unregisterAll(this), 10);
    }

    public void onClick(final @NotNull InventoryClickEvent event) {}

    @NotNull
    public final Player player() {
        return player;
    }

    @NotNull
    public final MenuParser<M> parser() {
        return parser;
    }
}
