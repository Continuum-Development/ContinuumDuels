package dev.continuum.duels.command.impl;

import dev.continuum.duels.arena.*;
import dev.continuum.duels.command.CommandRequirements;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.kit.PremadeKits;
import dev.continuum.duels.permission.DuelPermission;
import dev.continuum.duels.util.MaterialUtils;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.command.CommandResult;
import dev.manere.utils.command.impl.Commands;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.command.impl.suggestions.Suggestions;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.model.Tuple;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dev.manere.utils.command.CommandResult.done;
import static dev.manere.utils.command.CommandResult.stop;

public class ArenaCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("arenas")
            .info(info -> {
                info.description("Manages arenas.");
                info.namespace("duels");
            })
            .requirement(CommandRequirements::requirePlayer)
            .requirement(
                ctx -> CommandRequirements.requirePermission(
                    ctx,
                    DuelPermission.ARENA_COMMAND
                )
            )
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        // POS:
        // -1     0        1       2       3           4
        // SIZE:
        // 0      1        2       3       4           5
        //
        // arenas create   <arena>
        // arenas delete   <arena>
        // arenas teleport <arena>
        // arenas edit     <arena> info    displayname <display name>
        // arenas edit     <arena> info    icon        <icon>
        // arenas edit     <arena> corners 1
        // arenas edit     <arena> corners 2
        // arenas edit     <arena> center
        // arenas edit     <arena> spawn   1
        // arenas edit     <arena> spawn   2

        final Player player = ctx.player();
        final List<String> args = ctx.rawArgs();

        if (args.size() > 4 && args.get(2).equalsIgnoreCase("displayname")) {
            final String rawArena = args.get(1);
            final Arena arena = Arenas.arena(rawArena);

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        rawArena
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            arena.loadAsync();

            final List<String> arguments = new ArrayList<>();

            for (int position = 4; position < args.size(); position++) {
                final String argument = args.get(position);

                arguments.add(argument);
            }

            final String displayName = String.join(" ", arguments);
            arena.info().displayName(displayName);
            arena.saveAsync()
                .thenRun(() -> Messages.message("saving_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }));

            return stop();
        }

        if (args.size() > 4 && args.get(2).equalsIgnoreCase("icon")) {
            final String rawArena = args.get(1);
            final Arena arena = Arenas.arena(rawArena);

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        rawArena
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            arena.loadAsync();

            final List<String> iconArguments = new ArrayList<>();

            for (int position = 4; position < args.size(); position++) {
                final String argument = args.get(position);

                iconArguments.add(argument);
            }

            final String joined = String.join(" ", iconArguments);

            final Material material = MaterialUtils.parseOrNull(joined);

            if (material == null) {
                Messages.message("icon_does_not_exist", player, replacements -> {
                    replacements.element(Tuple.tuple("value", joined));
                    return replacements;
                });

                return stop();
            }

            arena.info().icon(material);
            arena.saveAsync()
                .thenRun(() -> Messages.message("saving_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }));

            return stop();
        }

        if (args.size() == 4 && args.get(2).equalsIgnoreCase("spawn")) {
            final String rawArena = args.get(1);
            final Arena arena = Arenas.arena(rawArena);

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        rawArena
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            arena.loadAsync();

            final String rawSpawn = args.get(3);

            int spawn = 0;

            if (rawSpawn.equalsIgnoreCase("1")) spawn = 1;
            if (rawSpawn.equalsIgnoreCase("2")) spawn = 2;

            if (spawn == 0) {
                Messages.message("usages.arena_command", player);
                return stop();
            }

            final ArenaSpawn arenaSpawn = spawn == 1 ? ArenaSpawn.ONE : ArenaSpawn.TWO;

            arena.spawn(arenaSpawn, player.getLocation());
            arena.saveAsync()
                .thenRun(() -> Messages.message("saving_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }));

            return stop();
        }

        if (args.size() == 3 && args.get(2).equalsIgnoreCase("center")) {
            final String rawArena = args.get(1);
            final Arena arena = Arenas.arena(rawArena);

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        rawArena
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            arena.loadAsync();

            arena.center(player.getLocation());
            arena.saveAsync()
                .thenRun(() -> Messages.message("saving_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }));

            return stop();
        }

        if (args.size() == 4 && args.get(2).equalsIgnoreCase("corners")) {
            final String rawArena = args.get(1);
            final Arena arena = Arenas.arena(rawArena);

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        rawArena
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            arena.loadAsync();

            final String rawCorner = args.get(3);

            int corner = 0;

            if (rawCorner.equalsIgnoreCase("1")) corner = 1;
            if (rawCorner.equalsIgnoreCase("2")) corner = 2;

            if (corner == 0) {
                Messages.message("usages.arena_command", player);
                return stop();
            }

            final ArenaCorner arenaCorner = corner == 1 ? ArenaCorner.ONE : ArenaCorner.TWO;

            arena.corner(arenaCorner, player.getLocation());
            arena.saveAsync()
                .thenRun(() -> Messages.message("saving_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_arena", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arena.name()
                    ));
                    return replacements;
                }));

            return stop();
        }

        if (args.size() == 2) {
            final String action = args.get(0);
            if (action.equalsIgnoreCase("create")) {
                final String rawArena = args.get(1);

                if (Arenas.arena(rawArena) != null) {
                    Messages.message("arena_already_exists", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            rawArena
                        ));
                        return replacements;
                    });

                    return CommandResult.stop();
                }

                if (rawArena.matches(".*[A-Z].*")) {
                    Messages.findAndSend("no_uppercase_letters", player);
                    return stop();
                }

                if (rawArena.matches(".*\\d.*")) {
                    Messages.findAndSend("no_numbers", player);
                    return stop();
                }

                if (!rawArena.matches("[a-zA-Z0-9_-]*")) {
                    Messages.findAndSend("no_special_symbols", player);
                    return stop();
                }

                final PremadeArena arena = new PremadeArena(rawArena);
                new Arenas().cached().element(arena);
                arena.saveAsync()
                    .thenRun(() -> Messages.message("creating_arena", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }))
                    .whenComplete(($, $$) -> Messages.message("created_arena", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }));

                return CommandResult.stop();
            } else if (action.equalsIgnoreCase("delete")) {
                final String rawArena = args.get(1);
                final Arena arena = Arenas.arena(rawArena);

                if (arena == null) {
                    Messages.message("arena_not_found", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            rawArena
                        ));
                        return replacements;
                    });

                    return CommandResult.stop();
                }

                arena.loadAsync();

                arena.deleteAsync()
                    .thenRun(() -> Messages.message("deleting_arena", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }))
                    .whenComplete(($, $$) -> Messages.message("deleted_arena", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }));

                return CommandResult.done();
            } else if (action.equalsIgnoreCase("teleport")) {
                final String rawArena = args.get(1);
                final Arena arena = Arenas.arena(rawArena);

                if (arena == null) {
                    Messages.message("arena_not_found", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            rawArena
                        ));
                        return replacements;
                    });

                    return CommandResult.stop();
                }

                arena.loadAsync();

                final Location center = arena.center();

                if (center == null) {
                    Messages.message("center_not_set", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    });

                    return CommandResult.stop();
                }

                player.teleportAsync(center, PlayerTeleportEvent.TeleportCause.PLUGIN)
                    .thenRun(() -> Messages.message("teleporting_to_center", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }))
                    .whenComplete(($, $$) -> Messages.message("teleported_to_center", player, replacements -> {
                        replacements.element(Tuple.tuple(
                            "arena",
                            arena.name()
                        ));
                        return replacements;
                    }));

                return stop();
            }
        }

        return CommandResult.done();
    }

    @Nullable
    @Override
    public Suggestions suggest(final @NotNull CommandContext ctx) {
        if (CommandRequirements.requirePlayer(ctx)) return null;

        return Suggestions.of();
    }
}
