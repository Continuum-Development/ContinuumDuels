package dev.continuum.duels.command;

import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.kit.premade.PremadeKits;
import dev.continuum.duels.menu.DuelMenu;
import dev.continuum.duels.parser.menu.Menus;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.command.CommandResult;
import dev.manere.utils.command.impl.Commands;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.command.impl.requirements.Requirements;
import dev.manere.utils.command.impl.suggestions.Suggestion;
import dev.manere.utils.command.impl.suggestions.Suggestions;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.model.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuelCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("duel")
            .info(info -> info.permission("continuum.duels.commands.duel"))
            .requirement(Requirements.playerOnly(ctx -> Messages.findAndSend("player_only", ctx.sender())))
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        final Player player = ctx.player();
        final int size = ctx.argSize();

        if (size == 0) {
            Messages.message("usage.duel", player, $ -> $);
            return CommandResult.stop();
        }

        final String rawTarget = ctx.rawArgAt(0);

        if (rawTarget == null) {
            Messages.message("usage.duel", player, $ -> Elements.of());
            return CommandResult.stop();
        }

        final Player target = Bukkit.getPlayerExact(rawTarget);

        if (target == null) {
            Messages.message("player_not_found", player, replacements -> Elements.of(Tuple.tuple("player", rawTarget)));
            return CommandResult.stop();
        }

        // TODO: duel menu
        Menus.menu(DuelMenu.class, player, target, 1);
        return CommandResult.stop();
    }

    @Nullable
    @Override
    public Suggestions suggest(final @NotNull CommandContext ctx) {
        if (!ctx.senderIsPlayer()) return Suggestions.players();

        if (ctx.size() == 1) return Suggestions
            .playersWithout(ctx.player())
            .add(Suggestion.text("<player>"));

        return Suggestions.playersWithout(ctx.player());
    }
}
