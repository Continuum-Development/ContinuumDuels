package dev.continuum.duels.parser.menu;
;
import dev.manere.utils.registration.Registrar;
import dev.manere.utils.scheduler.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ParsedMenu implements Listener {
    protected final Player player;
    protected final MenuParser parser;

    public ParsedMenu(final @NotNull Player player, final @NotNull String fileName) {
        this.player = player;
        this.parser = MenuParser.wrap(Menus.load(fileName));
        this.parser.menu();
        init();
        Registrar.events(this);
        this.parser.menu().open(player);
    }

    public abstract void init();

    @EventHandler
    public final void execute(final @NotNull InventoryCloseEvent event) {
        if (!event.getInventory().equals(parser.menu().getInventory())) return;

        onClose(event);

        Schedulers.async().execute(() -> HandlerList.unregisterAll(this), 10);
    }

    public void onClose(final @NotNull InventoryCloseEvent event) {}

    @NotNull
    public final Player player() {
        return player;
    }

    @NotNull
    public final MenuParser parser() {
        return parser;
    }
}
