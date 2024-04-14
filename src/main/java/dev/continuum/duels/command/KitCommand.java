package dev.continuum.duels.command;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.config.Messages;
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
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("duelkit")
            .info(info -> info.permission("continuum.duels.commands.duelkit"))
            .requirement(Requirements.playerOnly(ctx -> Messages.findAndSend("player_only", ctx.sender())))
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        final Player player = ctx.player();
        final int size = ctx.argSize();

        if (size == 0 || size == 1) {
            Messages.message("usage.duelkit", player, placeholders -> Elements.of());
            return CommandResult.stop();
        }

        if (size == 2) {
            final String action = ctx.rawArgs().get(0);
            final String kitName = ctx.rawArgs().get(1);

            if (action.equals("create")) {
                if (PremadeKits.kit(kitName) != null) {
                    Messages.message("kit_already_exists", player, placeholders -> {
                        placeholders.element(Tuple.tuple("kit", kitName));
                        return placeholders;
                    });

                    return CommandResult.stop();
                }

                PremadeKits.create(PremadeKit.kit(kitName));
                Messages.message("kit_created", player, placeholders -> {
                    placeholders.element(Tuple.tuple("kit", kitName));
                    return placeholders;
                });

                return CommandResult.stop();
            } else if (action.equals("delete")) {
                final PremadeKit kit = PremadeKits.kit(kitName);

                if (kit == null) {
                    Messages.message("kit_not_found", player, placeholders -> {
                        placeholders.element(Tuple.tuple("kit", kitName));
                        return placeholders;
                    });

                    return CommandResult.stop();
                }

                PremadeKits.delete(kit);
                Messages.message("kit_deleted", player, placeholders -> {
                    placeholders.element(Tuple.tuple("kit", kit.name()));
                    return placeholders;
                });

                return CommandResult.stop();
            } else if (action.equals("info")) {
                final PremadeKit kit = PremadeKits.kit(kitName);

                if (kit == null) {
                    Messages.message("kit_not_found", player, elements -> {
                        elements.element(Tuple.tuple("kit", kitName));
                        return elements;
                    });

                    return CommandResult.stop();
                }

                Messages.message("kit_info", player, placeholders -> {
                    placeholders.element(Tuple.tuple("name", kit.name()));
                    placeholders.element(Tuple.tuple("display_name", kit.displayName()));
                    placeholders.element(Tuple.tuple("icon", StringUtils.capitalize(kit.icon().toString())));
                    placeholders.element(Tuple.tuple("editable", editable(kit)));
                    return placeholders;
                });

                return CommandResult.stop();
            }
        }

        return CommandResult.stop();
    }

    @NotNull
    public String editable(final @NotNull PremadeKit kit) {
        final boolean editable = kit.editable();
        return editable ? "<#64ff5c>✔" : "<#ff4766>❌";
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
