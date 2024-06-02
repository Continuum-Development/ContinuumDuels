package dev.continuum.duels;

import dev.continuum.duels.arena.PremadeArenas;
import dev.continuum.duels.database.provider.DatabaseProviders;
import dev.continuum.duels.elo.PlayerRatings;
import dev.continuum.duels.fight.FrozenPlayers;
import dev.continuum.duels.kit.PremadeKits;
import dev.continuum.duels.lobby.Lobby;
import dev.continuum.duels.util.CacheAction;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.SavableCache;
import dev.manere.utils.library.wrapper.PluginWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public final class ContinuumDuels extends PluginWrapper {
    @Override
    protected void start() {
        saveDefaultConfig();
        saveConfig();
        Files.save("messages.yml");
        Files.save("lobby.yml");

        DatabaseProviders.kits().start();
        DatabaseProviders.statistics().start();

        SavableCache.cache(CacheAction.START, PremadeKits.class, "Premade Kits");
        SavableCache.cache(CacheAction.START, PremadeArenas.class, "Premade Arenas");
        SavableCache.cache(CacheAction.START, PlayerRatings.class, "Player Ratings (ELO)");
        Lobby.lobby().load(true);

        events().register(PlayerMoveEvent.class, event -> {
            final Player player = event.getPlayer();

            if (FrozenPlayers.has(player)) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    protected void stop() {
        DatabaseProviders.kits().stop();
        DatabaseProviders.statistics().stop();

        SavableCache.cache(CacheAction.STOP, PremadeKits.class, "Premade Kits");
        SavableCache.cache(CacheAction.STOP, PremadeArenas.class, "Premade Arenas");
        SavableCache.cache(CacheAction.STOP, PlayerRatings.class, "Player Ratings (ELO)");
        new Lobby().save(true);
    }
}
