package dev.continuum.duels.command;

import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.elements.impl.ElementsImpl;

public class CommandRegistry {
    private static final Elements<Class<? extends AbstractCommand>> commands = new ElementsImpl<>() {{ elements(
        DuelCommand.class
    ); }};

    public static void register() {
        for (final Class<? extends AbstractCommand> command : commands) {
            AbstractCommand.register(command);
        }
    }
}
