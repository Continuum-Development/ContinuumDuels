package dev.continuum.duels;

import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.kit.premade.PremadeKits;
import dev.continuum.duels.util.Files;
import dev.manere.utils.library.wrapper.PluginWrapper;

public final class ContinuumDuels extends PluginWrapper {
    @Override
    protected void start() {
        Arenas.start();
        PremadeKits.start();

        Files.save("config.yml");
        Files.save("messages.yml");
    }

    @Override
    protected void stop() {
        Arenas.stop();
        PremadeKits.stop();
    }
}
