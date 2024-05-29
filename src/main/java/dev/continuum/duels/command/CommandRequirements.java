package dev.continuum.duels.command;

import dev.continuum.duels.config.Messages;
import dev.continuum.duels.permission.DuelPermission;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.model.Tuple;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.sound.midi.MidiFileFormat;

public class CommandRequirements {
    public static boolean requirePlayer(final @NotNull CommandContext ctx) {
        if (!ctx.senderIsPlayer()) {
            Messages.message("player_only", ctx.sender());
            return true;
        }

        return false;
    }

    public static boolean requirePermission(final @NotNull CommandContext ctx, final @NotNull DuelPermission permission) {
        if (!ctx.sender().hasPermission(permission.key())) {
            Messages.message("no_permission", ctx.sender(), replacements -> {
                replacements.element(Tuple.tuple(
                    "permission",
                    permission.key()
                ));
                return replacements;
            });
            return true;
        }

        return false;
    }
}
