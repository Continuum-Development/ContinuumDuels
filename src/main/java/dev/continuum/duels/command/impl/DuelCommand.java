package dev.continuum.duels.command.impl;

import dev.continuum.duels.command.CommandRequirements;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.menu.DuelMenu;
import dev.continuum.duels.permission.DuelPermission;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.command.CommandResult;
import dev.manere.utils.command.impl.Commands;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.player.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuelCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("duel")
            .info(info -> {
                info.description("Sends a duel request. Wait, actually no it doesn't! This command only opens the duel menu.");
                info.namespace("duels");
                info.aliases("1v1");
            })
            .requirement(CommandRequirements::requirePlayer)
            .requirement(
                ctx -> CommandRequirements.requirePermission(ctx,
                    DuelPermission.DUEL_COMMAND
                )
            )
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        final Player sender = ctx.player();
        final List<String> args = ctx.rawArgs();

        if (args.size() != 1) {
            Messages.message("usages.duel_command", sender);
            return CommandResult.stop();
        }

        final String name = args.get(0);
        final Player target = Bukkit.getPlayerExact(name);

        if (target == null) {
            Messages.message("player_not_found", sender, replacements -> {
                replacements.element(Tuple.tuple("player", name));
                return replacements;
            });

            return CommandResult.stop();
        }

        DuelMenu.open(sender, target);
        return CommandResult.stop();
    }
}
