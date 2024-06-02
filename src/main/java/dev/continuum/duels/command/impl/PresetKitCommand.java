package dev.continuum.duels.command.impl;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.PremadeArena;
import dev.continuum.duels.arena.PremadeArenas;
import dev.continuum.duels.command.CommandRequirements;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.kit.PremadeKit;
import dev.continuum.duels.kit.PremadeKits;
import dev.continuum.duels.permission.DuelPermission;
import dev.continuum.duels.util.MaterialUtils;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.command.AbstractCommand;
import dev.manere.utils.command.CommandResult;
import dev.manere.utils.command.impl.Commands;
import dev.manere.utils.command.impl.dispatcher.CommandContext;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.model.Tuple;
import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.manere.utils.command.CommandResult.stop;

public class PresetKitCommand extends AbstractCommand {
    @NotNull
    @Override
    public Commands command() {
        return Commands.command("presetkits")
            .info(info -> {
                info.description("Manages premade/preset kits.");
                info.namespace("duels");
                info.aliases("premadekits");
            })
            .requirement(CommandRequirements::requirePlayer)
            .requirement(
                ctx -> CommandRequirements.requirePermission(ctx,
                    DuelPermission.PRESET_KIT_COMMAND
                )
            )
            .executes(this)
            .completes(this);
    }

    @NotNull
    @Override
    public CommandResult run(final @NotNull CommandContext ctx) {
        final Player player = ctx.player();
        final List<String> args = ctx.rawArgs();

        // POS:
        // -1     0        1       2       3           4
        // SIZE:
        // 0      1        2       3       4           5
        //
        // presetkits edit     <kit> info     icon        <icon with spaces support>
        // presetkits edit     <kit> import   contents
        // presetkits edit     <kit> arenas   add         <arena>
        // presetkits edit     <kit> arenas   remove      <arena>
        // presetkits edit     <kit> arenas   list        <arena>
        // presetkits edit     <kit>

        if (args.size() == 2 && args.get(0).equalsIgnoreCase("create")) {
            final String kitName = args.get(1);

            if (PremadeKits.kit(kitName) != null) {
                Messages.message("kit_already_exists", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            if (kitName.matches(".*[A-Z].*")) {
                Messages.findAndSend("no_uppercase_letters", player);
                return stop();
            }

            if (kitName.matches(".*\\d.*")) {
                Messages.findAndSend("no_numbers", player);
                return stop();
            }

            if (!kitName.matches("[a-zA-Z0-9_-]*")) {
                Messages.findAndSend("no_special_symbols", player);
                return stop();
            }

            final PremadeKit kit = PremadeKit.kit(kitName);
            new PremadeKits().cached().element(kit);
            kit.saveAsync()
                .thenRun(() -> Messages.message("creating_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("created_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() == 2 && args.get(0).equalsIgnoreCase("delete")) {
            final String kitName = args.get(1);
            final PremadeKit kit = PremadeKits.kit(kitName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            kit.loadAsync();

            kit.deleteAsync()
                .thenRun(() -> Messages.message("deleting_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("deleted_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() >= 5 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("info") && args.get(3).equalsIgnoreCase("displayname")) {
            final String kitName = args.get(1);
            final String displayName = String.join(" ", args.subList(4, args.size()));
            final PremadeKit kit = PremadeKits.kit(kitName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            kit.loadAsync();

            kit.displayName(displayName);

            kit.saveAsync()
                .thenRun(() -> Messages.message("saving_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() >= 5 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("info") && args.get(3).equalsIgnoreCase("icon")) {
            final String kitName = args.get(1);
            final String iconName = String.join(" ", args.subList(4, args.size()));
            final Material material = MaterialUtils.parseOrNull(iconName);
            final PremadeKit kit = PremadeKits.kit(kitName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            kit.loadAsync();

            if (material == null) {
                Messages.message("icon_does_not_exist", player, replacements -> {
                    replacements.element(Tuple.tuple("value", iconName));
                    return replacements;
                });

                return stop();
            }

            kit.icon(material);

            kit.saveAsync()
                .thenRun(() -> Messages.message("saving_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() == 4 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("import") && args.get(3).equalsIgnoreCase("contents")) {
            final String kitName = args.get(1);
            final PremadeKit kit = PremadeKits.kit(kitName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return CommandResult.stop();
            }

            kit.loadAsync();

            kit.contents(() -> {
                final ItemStack[] array = player.getInventory().getContents();
                final Cachable<Integer, ItemStack> contents = Cachable.of();

                for (int index = 0; index < 41; index++) {
                    final ItemStack item = ObjectUtils.defaultIfNull(
                        array[index],
                        new ItemStack(Material.AIR)
                    );

                    contents.cache(index, item);
                }

                contents.cache(40, player.getInventory().getItemInOffHand());
                return contents;
            });

            kit.saveAsync()
                .thenRun(() -> Messages.message("saving_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() == 5 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("arenas") && args.get(3).equalsIgnoreCase("add")) {
            final String kitName = args.get(1);
            final String arenaName = args.get(4);

            final PremadeKit kit = PremadeKits.kit(kitName);
            final PremadeArena arena = PremadeArenas.arena(arenaName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return stop();
            }

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arenaName
                    ));
                    return replacements;
                });

                return stop();
            }

            kit.loadAsync();
            arena.loadAsync();

            final Elements<Arena> arenas = kit.arenas();

            arenas.element(arena);

            kit.arenas(arenas);

            kit.saveAsync()
                .thenRun(() -> Messages.message("saving_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() == 5 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("arenas") && args.get(3).equalsIgnoreCase("remove")) {
            final String kitName = args.get(1);
            final String arenaName = args.get(4);

            final PremadeKit kit = PremadeKits.kit(kitName);
            final PremadeArena arena = PremadeArenas.arena(arenaName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return stop();
            }

            if (arena == null) {
                Messages.message("arena_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arenaName
                    ));
                    return replacements;
                });

                return stop();
            }

            kit.loadAsync();
            arena.loadAsync();

            final Elements<Arena> arenas = kit.arenas();

            if (arenas.has(arena)) {
                Messages.message("arena_not_linked", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "arena",
                        arenaName
                    ));
                    return replacements;
                });
                return stop();
            }

            arenas.del(arena);

            kit.arenas(arenas);

            kit.saveAsync()
                .thenRun(() -> Messages.message("saving_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }))
                .whenComplete(($, $$) -> Messages.message("saved_kit", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kit.name()
                    ));
                    return replacements;
                }));

            return CommandResult.stop();
        }

        if (args.size() == 4 && args.get(0).equalsIgnoreCase("edit") && args.get(2).equalsIgnoreCase("arenas") && args.get(3).equalsIgnoreCase("list")) {
            final String kitName = args.get(1);

            final PremadeKit kit = PremadeKits.kit(kitName);

            if (kit == null) {
                Messages.message("kit_not_found", player, replacements -> {
                    replacements.element(Tuple.tuple(
                        "kit",
                        kitName
                    ));
                    return replacements;
                });

                return stop();
            }

            kit.loadAsync();

            Messages.message("linked_arenas", player, replacements -> {
                final Elements<Arena> arenas = kit.arenas();
                final Elements<String> arenaNames = Elements.of();

                for (final Arena arena : arenas) {
                    arenaNames.element(arena.name());
                }

                final String arenasRepr = arenaNames.elements().toString()
                    .replaceAll("]", "")
                    .replaceAll("\\[", "");

                replacements.element(Tuple.tuple("arenas", arenasRepr));
                replacements.element(Tuple.tuple("amount", String.valueOf(arenas.size())));
                return replacements;
            });

            return CommandResult.stop();
        }

        Messages.message("usages.premade_kit_command", player);
        return CommandResult.stop();
    }
}
