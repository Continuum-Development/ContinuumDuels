package dev.continuum.duels.command;

import dev.continuum.duels.command.impl.*;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.elements.Elements;
import org.jetbrains.annotations.NotNull;

public class CommandRegistry {
    private final Elements<@NotNull Class<? extends @NotNull AbstractCommand>> commands = Elements.of(
        ArenaCommand.class,
        PresetKitCommand.class,
        DuelCommand.class
    );

    public void register(final @NotNull Class<? extends AbstractCommand> command) {
        AbstractCommand.register(command);
    }

    public void register() {
        for (final @NotNull Class<? extends AbstractCommand> command : commands) register(command);
    }

    @NotNull
    public Elements<@NotNull Class<? extends @NotNull AbstractCommand>> commands() {
        return commands;
    }
}
