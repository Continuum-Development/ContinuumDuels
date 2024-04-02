package dev.continuum.duels.command;

import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.game.duel.Duel;
import dev.continuum.duels.kit.premade.PremadeKit;
import dev.continuum.duels.kit.premade.PremadeKits;
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
            .requirement(Requirements.playerOnly(ctx -> {
                Messages.findAndSend("player_only", ctx.sender());
            }))
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        final Player player = ctx.player();
        final int size = ctx.argSize();

        if (size == 0) {
            Messages.message("usage.duel", player, $ -> Elements.of());
            return CommandResult.stop();
        }

        if (size == 1) {
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
            return CommandResult.stop();
        }

        if (size == 2) {
            final String rawTarget = ctx.rawArgAt(0);
            final String rawKit = ctx.rawArgAt(1);

            if (rawKit == null || rawTarget == null) {
                Messages.message("usage.duel", player, $ -> Elements.of());
                return CommandResult.stop();
            }

            final Player target = Bukkit.getPlayerExact(rawTarget);

            if (target == null) {
                Messages.message("player_not_found", player, replacements -> Elements.of(Tuple.tuple("player", rawTarget)));
                return CommandResult.stop();
            }

            // TODO: duel menu
            return CommandResult.stop();
        }

        if (size == 3) {
            final String rawTarget = ctx.rawArgAt(0);
            final String rawKit = ctx.rawArgAt(1);
            final String rawRounds = ctx.rawArgAt(2);

            if (rawTarget == null || rawKit == null || rawRounds == null) {
                Messages.message("usage.duel", player, $ -> Elements.of());
                return CommandResult.stop();
            }

            final Player target = Bukkit.getPlayerExact(rawTarget);

            if (target == null) {
                Messages.message("player_not_found", player, replacements -> Elements.of(Tuple.tuple("player", rawTarget)));
                return CommandResult.stop();
            }

            // TODO: send duel with random arena
            return CommandResult.stop();
        }

        if (size == 4) {
            final String rawTarget = ctx.rawArgAt(0);
            final String rawKit = ctx.rawArgAt(1);
            final String rawRounds = ctx.rawArgAt(2);
            final String rawArena = ctx.rawArgAt(3);

            if (rawTarget == null || rawKit == null || rawRounds == null || rawArena == null) {
                Messages.message("usage.duel", player, $ -> Elements.of());
                return CommandResult.stop();
            }

            final Player target = Bukkit.getPlayerExact(rawTarget);

            if (target == null) {
                Messages.message("player_not_found", player, replacements -> Elements.of(Tuple.tuple("player", rawTarget)));
                return CommandResult.stop();
            }



            // TODO: send duel
            return CommandResult.stop();
        }
        
        Messages.message("usage.duel", player, $ -> Elements.of());
        return CommandResult.stop();
    }

    @Nullable
    @Override
    public Suggestions suggest(final @NotNull CommandContext ctx) {
        if (!ctx.senderIsPlayer()) return Suggestions.players();

        if (ctx.size() == 1) return Suggestions.playersWithout(ctx.player()).add(Suggestion.text("<player>"));
        if (ctx.size() == 2) return PremadeKits.suggestions().add(Suggestion.text("<kit >"));
        if (ctx.size() == 3) return Suggestions.numbers(20).add(Suggestion.text("<rounds>"));
        if (ctx.size() == 4) return Arenas.suggestions().add(Suggestion.text("<arena>"));

        return Suggestions.playersWithout(ctx.player());
    }
}
