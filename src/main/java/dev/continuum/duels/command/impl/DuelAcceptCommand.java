package dev.continuum.duels.command.impl;

import dev.continuum.duels.config.Messages;
import dev.continuum.duels.duel.DuelRequest;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.command.CommandResult;
import dev.manere.utils.command.impl.CommandInfo;
import dev.manere.utils.command.impl.Commands;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.model.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelAcceptCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("duelaccept")
            .info(info -> info.aliases("acceptduel"))
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        if (ctx.rawArgs().size() > 1) {
            Messages.message("usages.duel_accept_command", ctx.player());
            return CommandResult.done();
        }

        if (ctx.rawArgs().isEmpty()) {
            for (final DuelRequest request : DuelRequest.requests()) {
                if (request.target().getUniqueId().equals(ctx.player().getUniqueId())) {
                    request.accept();
                    return CommandResult.done();
                }
            }

            Messages.message("duel_request_not_found", ctx.player());
        } else {
            final String name = ctx.rawArgs().get(0);

            if (name == null) {
                Messages.message("usages.duel_accept_command", ctx.player());
                return CommandResult.done();
            }

            final Player target = Bukkit.getPlayerExact(name);

            if (target == null) {
                Messages.message("player_not_found", ctx.player(), elements -> {
                    elements.element(Tuple.tuple("player", name));
                    return elements;
                });

                return CommandResult.done();
            }

            final DuelRequest request = DuelRequest.findBySender(target);

            if (request == null) {
                Messages.message("duel_request_not_found", ctx.player());
                return CommandResult.done();
            }

            request.accept();
        }

        return CommandResult.done();
    }
}
