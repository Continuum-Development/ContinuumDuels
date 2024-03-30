package dev.continuum.duels;

import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.kit.premade.PremadeKits;
import dev.manere.utils.library.wrapper.PluginWrapper;

public final class ContinuumDuels extends PluginWrapper {
    @Override
    protected void start() {
        Arenas.start();
        PremadeKits.start();
    }

    @Override
    protected void stop() {
        Arenas.stop();
        PremadeKits.stop();
    }
}
