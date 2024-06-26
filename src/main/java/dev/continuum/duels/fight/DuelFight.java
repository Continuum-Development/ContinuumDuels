package dev.continuum.duels.fight;

import dev.continuum.duels.arena.ArenaCorner;
import dev.continuum.duels.arena.ArenaSpawn;
import dev.continuum.duels.arena.TemporaryArena;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.kit.Kit;
import dev.continuum.duels.lobby.Lobby;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.player.PlayerUtils;
import dev.manere.utils.prettify.ListPrettify;
import dev.manere.utils.scheduler.Schedulers;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DuelFight implements Fight<DuelTeam> {
    private final TemporaryArena arena;
    private final Kit kit;
    private final Player one;
    private final Player two;

    private final Cachable<UUID, DuelTeam> teams = Cachable.of();

    private final Cachable<DuelTeam, Integer> wins = Cachable.of();
    private final Cachable<DuelTeam, Integer> losses = Cachable.of();
    private final int rounds;
    private final Set<Player> spectators = new HashSet<>();
    private boolean ended = false;
    private int currentRound = 1;
    private long roundStartTime = 0L;
    private long fightStartTime = 0L;

    public DuelFight(final @NotNull Kit kit, final @NotNull TemporaryArena arena, final @NotNull Player one, final @NotNull Player two, final int rounds) {
        this.kit = kit;
        this.arena = arena;
        this.one = one;
        this.two = two;
        this.rounds = rounds;
    }

    @NotNull
    public DuelTeam opponent(final @NotNull DuelTeam team) {
        return team.equals(DuelTeam.RED) ? DuelTeam.BLUE : DuelTeam.RED;
    }

    @Override
    public void handleDeath(final @NotNull Player dead, final @NotNull Location deathLocation) {
        final Player winner = opponent(dead);

        dead.teleportAsync(deathLocation)
            .whenComplete(($, $$) -> {
                dead.setGameMode(GameMode.SPECTATOR);
                dead.setWorldBorder(worldBorder());
            });

        for (final Player player : playersAndSpectators()) {
            if (player.getUniqueId().equals(dead.getUniqueId())) continue;

            Messages.message("player_died", player, replacements -> {
                replacements.element(Tuple.tuple("player", dead.getName()));
                return replacements;
            });
        }

        endRound(winner);
    }

    @Override
    public void leave(final @NotNull Player player) {
        if (spectators().contains(player)) {
            if (!stopSpectating(player)) throw new RuntimeException();
            return;
        }

        handleDeath(player, player.getLocation());
    }

    @Override
    public boolean endFight() {
        final Location spawnOne = arena.spawn(ArenaSpawn.ONE);
        final Location spawnTwo = arena.spawn(ArenaSpawn.TWO);
        final Location center = arena.center();

        if (spawnOne == null || spawnTwo == null || center == null) return false;

        final int redWins = wins(DuelTeam.RED);
        final int blueWins = wins(DuelTeam.BLUE);

        if (redWins == blueWins) {
            for (final Player player : playersAndSpectators()) {
                Messages.message("fight_tie_ended", player, (placeholders) -> {
                    placeholders.element(Tuple.tuple("red", one.getName()));
                    placeholders.element(Tuple.tuple("blue", two.getName()));
                    placeholders.element(Tuple.tuple("winner", "N/A (Tie)"));
                    placeholders.element(Tuple.tuple("loser", "N/A (Tie)"));
                    placeholders.element(Tuple.tuple("kit", kit.displayName()));
                    placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                    placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                    placeholders.element(Tuple.tuple("total_duration", DurationFormatUtils.formatDuration(totalDuration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("round_duration", DurationFormatUtils.formatDuration(duration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("red_wins", String.valueOf(redWins)));
                    placeholders.element(Tuple.tuple("blue_wins", String.valueOf(blueWins)));

                    return placeholders;
                });
            }
        } else if (redWins > blueWins) {
            for (final Player player : playersAndSpectators()) {
                Messages.message("fight_ended", player, (placeholders) -> {
                    placeholders.element(Tuple.tuple("red", one.getName()));
                    placeholders.element(Tuple.tuple("blue", two.getName()));
                    placeholders.element(Tuple.tuple("winner", one.getName()));
                    placeholders.element(Tuple.tuple("loser", two.getName()));
                    placeholders.element(Tuple.tuple("kit", kit.displayName()));
                    placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                    placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                    placeholders.element(Tuple.tuple("total_duration", DurationFormatUtils.formatDuration(totalDuration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("round_duration", DurationFormatUtils.formatDuration(duration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("red_wins", String.valueOf(redWins)));
                    placeholders.element(Tuple.tuple("blue_wins", String.valueOf(blueWins)));

                    return placeholders;
                });
            }
        } else {
            for (final Player player : playersAndSpectators()) {
                Messages.message("fight_ended", player, (placeholders) -> {
                    placeholders.element(Tuple.tuple("red", one.getName()));
                    placeholders.element(Tuple.tuple("blue", two.getName()));
                    placeholders.element(Tuple.tuple("winner", two.getName()));
                    placeholders.element(Tuple.tuple("loser", one.getName()));
                    placeholders.element(Tuple.tuple("kit", kit.displayName()));
                    placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                    placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                    placeholders.element(Tuple.tuple("total_duration", DurationFormatUtils.formatDuration(totalDuration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("round_duration", DurationFormatUtils.formatDuration(duration(), "HH'h' mm'm' ss's'", false)));
                    placeholders.element(Tuple.tuple("red_wins", String.valueOf(redWins)));
                    placeholders.element(Tuple.tuple("blue_wins", String.valueOf(blueWins)));

                    return placeholders;
                });
            }
        }

        for (final Player player : playersAndSpectators()) {
            FrozenPlayers.unfreeze(player);
        }

        this.ended = true;

        Schedulers.sync().execute(() -> {
            for (final Player spectator : spectators()) {
                stopSpectating(spectator);
            }

            for (final Player player : playersAndSpectators()) {
                player.setInvulnerable(false);
                FrozenPlayers.unfreeze(player);

                Lobby.teleport(player);

                player.setHealth(20.0);
                player.setWorldBorder(null);
                player.setGameMode(GameMode.SURVIVAL);
                PlayerUtils.heal(player);
                PlayerUtils.clearPotionEffects(player);
            }

            arena.delete();
        }, 100);
        return true;
    }

    @Override
    public long totalDuration() {
        return System.currentTimeMillis() - fightStartTime;
    }

    @Override
    public boolean endRound(final @NotNull Player winner) {
        final Location spawnOne = arena.spawn(ArenaSpawn.ONE);
        final Location spawnTwo = arena.spawn(ArenaSpawn.TWO);
        final Location center = arena.center();

        if (spawnOne == null || spawnTwo == null || center == null) return false;

        final Player loser = opponent(winner);

        for (final Player player : playersAndSpectators()) {
            FrozenPlayers.unfreeze(player);

            Messages.message("round_ended", player, (placeholders) -> {
                final int redWins = wins(DuelTeam.RED);
                final int blueWins = wins(DuelTeam.BLUE);

                placeholders.element(Tuple.tuple("red", one.getName()));
                placeholders.element(Tuple.tuple("blue", two.getName()));
                placeholders.element(Tuple.tuple("winner", winner.getName()));
                placeholders.element(Tuple.tuple("loser", loser.getName()));
                placeholders.element(Tuple.tuple("kit", kit.displayName()));
                placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                placeholders.element(Tuple.tuple("total_duration", DurationFormatUtils.formatDuration(totalDuration(), "HH'h' mm'm' ss's'", false)));
                placeholders.element(Tuple.tuple("round_duration", DurationFormatUtils.formatDuration(duration(), "HH'h' mm'm' ss's'", false)));
                placeholders.element(Tuple.tuple("red_wins", String.valueOf(redWins)));
                placeholders.element(Tuple.tuple("blue_wins", String.valueOf(blueWins)));

                return placeholders;
            });
        }

        for (final Player player : spectators()) {
            player.teleportAsync(center).whenComplete(
                ($, $$) -> player.setGameMode(GameMode.SPECTATOR)
            );
        }

        one.setInvulnerable(true);

        final var ref = new Object() {
            boolean result = true;
        };

        Schedulers.sync().execute(() -> {
            one.setInvulnerable(false);

            if (!startRound()) {
                ref.result = false;
                return;
            }

            ref.result = true;
        }, 60);

        return ref.result;
    }

    @Override
    public boolean startRound() {
        roundStartTime = System.currentTimeMillis();

        final Location spawnOne = arena.spawn(ArenaSpawn.ONE);
        final Location spawnTwo = arena.spawn(ArenaSpawn.TWO);

        if (spawnOne == null || spawnTwo == null) return false;

        for (final Player player : playersAndSpectators()) {
            player.setWorldBorder(worldBorder());

            Messages.message("round_start_message", player, (placeholders) -> {
                placeholders.element(Tuple.tuple("red", one.getName()));
                placeholders.element(Tuple.tuple("blue", two.getName()));
                placeholders.element(Tuple.tuple("kit", kit.displayName()));
                placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                placeholders.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                placeholders.element(Tuple.tuple("round", String.valueOf(round())));

                return placeholders;
            });
        }

        one.teleportAsync(spawnOne).whenComplete(($, $$) -> {
            one.setHealth(20.0);
            one.setWorldBorder(worldBorder());
            one.setGameMode(GameMode.SURVIVAL);
            PlayerUtils.heal(one);
            PlayerUtils.clearPotionEffects(one);
            kit.contents().give(one);
            FrozenPlayers.freeze(one);
        });

        two.teleportAsync(spawnTwo).whenComplete(($, $$) -> {
            two.setHealth(20.0);
            two.setWorldBorder(worldBorder());
            two.setGameMode(GameMode.SURVIVAL);
            PlayerUtils.heal(two);
            PlayerUtils.clearPotionEffects(two);
            kit.contents().give(two);
            FrozenPlayers.freeze(two);
        });

        var ref = new Object() {
            int secondsLeft = 5;
        };

        Schedulers.sync().execute(task -> {
            if (ref.secondsLeft <= 0) {
                FrozenPlayers.unfreeze(one);
                FrozenPlayers.unfreeze(two);

                // message (started)
                Messages.message("started", one);
                Messages.message("started", two);

                task.cancel();
                return;
            }

            ref.secondsLeft--;

            // message (starting 5 sec)
            Messages.message("starting_soon", one, Elements.of(
                Tuple.tuple(
                    "seconds_left",
                    String.valueOf(ref.secondsLeft))
                )
            );

            Messages.message("starting_soon", two, Elements.of(
                Tuple.tuple(
                    "seconds_left",
                    String.valueOf(ref.secondsLeft))
                )
            );
        });

        return true;
    }

    @Override
    public boolean startFight() {
        fightStartTime = System.currentTimeMillis();
        roundStartTime = System.currentTimeMillis();

        final Location spawnOne = arena.spawn(ArenaSpawn.ONE);
        final Location spawnTwo = arena.spawn(ArenaSpawn.TWO);

        if (spawnOne == null || spawnTwo == null) return false;

        for (final Player player : playersAndSpectators()) {
            player.setWorldBorder(worldBorder());

            Messages.message("round_start_message", player, (placeholders) -> {
                placeholders.element(Tuple.tuple("red", one.getName()));
                placeholders.element(Tuple.tuple("blue", two.getName()));
                placeholders.element(Tuple.tuple("kit", kit.displayName()));
                placeholders.element(Tuple.tuple("arena", arena.info().displayName()));
                placeholders.element(Tuple.tuple("spectators", ListPrettify.players(new ArrayList<>(spectators()))));
                placeholders.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                placeholders.element(Tuple.tuple("round", String.valueOf(round())));

                return placeholders;
            });
        }

        for (final Player spectator : spectators()) {
            final Location center = arena.center();
            if (center == null) return false;

            spectator.teleportAsync(center);
        }

        one.teleport(spawnOne);
        two.teleport(spawnTwo);

        one.setHealth(20.0);
        two.setHealth(20.0);

        PlayerUtils.heal(one);
        PlayerUtils.heal(two);

        PlayerUtils.clearPotionEffects(one);
        PlayerUtils.clearPotionEffects(two);

        kit.contents().give(one);
        kit.contents().give(two);

        FrozenPlayers.freeze(one);
        FrozenPlayers.freeze(two);

        var ref = new Object() {
            int secondsLeft = 5;
        };

        Schedulers.sync().execute(task -> {
            if (ref.secondsLeft <= 0) {
                FrozenPlayers.unfreeze(one);
                FrozenPlayers.unfreeze(two);

                // message (started)
                Messages.message("started", one);
                Messages.message("started", two);

                task.cancel();
                return;
            }

            ref.secondsLeft--;

            // message (starting 5 sec)
            Messages.message("starting_soon", one, Elements.of(
                Tuple.tuple(
                    "seconds_left",
                    String.valueOf(ref.secondsLeft))
                )
            );

            Messages.message("starting_soon", two, Elements.of(
                Tuple.tuple(
                    "seconds_left",
                    String.valueOf(ref.secondsLeft))
                )
            );
        });

        return true;
    }

    /**
     * Returns false if balls. (center = null)
     */
    @Override
    public boolean spectate(final @NotNull Player spectator, final @Nullable Player target) {
        final Location center = arena.center();
        if (center == null) return false;

        final Location destination = target == null ? center : target.getLocation();

        spectators.add(spectator);

        spectator.teleportAsync(destination)
            .whenComplete(($, $$) -> {
                if (target == null) {
                    Messages.message("spectating_fight", spectator);
                } else {
                    Messages.message("spectating_player", spectator, replacements -> {
                        replacements.element(Tuple.tuple("player", target.getName()));
                        return replacements;
                    });
                }

                spectator.setWorldBorder(worldBorder());

                for (final Player player : playersAndSpectators()) {
                    if (target == null) {
                        Messages.message("player_is_now_spectating_this_fight", player, replacements -> {
                            replacements.element(Tuple.tuple("spectator", spectator.getName()));
                            return replacements;
                        });
                    } else {
                        Messages.message("player_is_now_spectating_target", player, replacements -> {
                            replacements.element(Tuple.tuple("spectator", spectator.getName()));
                            replacements.element(Tuple.tuple("target", target.getName()));
                            return replacements;
                        });
                    }
                }

                spectator.setGameMode(GameMode.SPECTATOR);
            });

        return true;
    }

    @NotNull
    public WorldBorder worldBorder() {
        final Location center = arena.center();
        final Location cornerOne = arena.corner(ArenaCorner.ONE);
        final Location cornerTwo = arena.corner(ArenaCorner.TWO);

        final WorldBorder border = Bukkit.createWorldBorder();

        if (center == null || cornerOne == null || cornerTwo == null || arena.borderSize() == -1) {
            return border;
        }

        border.setCenter(center);
        border.setDamageAmount(10);
        border.setDamageBuffer(1);
        border.setWarningDistance(3);
        border.setSize(arena.borderSize());

        return border;
    }

    @NotNull
    public Set<Player> playersAndSpectators() {
        final Set<Player> players = new HashSet<>(players());
        final Set<Player> spectators = new HashSet<>(spectators());
        final Set<Player> sum = new HashSet<>();

        sum.addAll(players);
        sum.addAll(spectators);

        return sum;
    }

    @Override
    public boolean stopSpectating(final @NotNull Player spectator) {
        if (!spectators.contains(spectator)) return false;

        spectators.remove(spectator);

        spectator.setGameMode(GameMode.SURVIVAL);

        spectator.setWorldBorder(null);

        Messages.message("stopped_spectating", spectator);

        for (final @NotNull Player player : playersAndSpectators()) {
            Messages.message("player_stopped_spectating", player, replacements -> {
                replacements.element(Tuple.tuple("spectator", spectator.getName()));
                return replacements;
            });
        }

        Lobby.teleport(spectator);
        return true;
    }

    @Override
    public boolean ended() {
        return ended;
    }

    @Override
    public boolean inProgress() {
        return !ended();
    }

    @Override
    public boolean ranked() {
        return false;
    }

    @Override
    public boolean friendlyFireAllowed() {
        return true;
    }

    @Override
    public long duration() {
        final long nowTime = System.currentTimeMillis();
        return nowTime - roundStartTime;
    }

    @Override
    public int rounds() {
        return rounds;
    }

    @Override
    public int round() {
        return currentRound;
    }

    @Override
    public int wins(final @NotNull DuelTeam team) {
        return ObjectUtils.defaultIfNull(wins.val(team), 0);
    }

    @Override
    public int losses(final @NotNull DuelTeam team) {
        return ObjectUtils.defaultIfNull(losses.val(team), 0);
    }

    @NotNull
    @Override
    public TemporaryArena arena() {
        return arena;
    }

    @NotNull
    @Override
    public Kit kit() {
        return kit;
    }

    @NotNull
    @Override
    public Set<Player> players() {
        return new HashSet<>(Set.of(one, two));
    }

    @NotNull
    @Override
    public Set<Player> spectators() {
        return spectators;
    }

    @NotNull
    @Override
    public Set<DuelTeam> teams() {
        return new HashSet<>(Set.of(DuelTeam.RED, DuelTeam.BLUE));
    }

    @NotNull
    public Player one() {
        return one;
    }

    @NotNull
    public Player two() {
        return two;
    }

    @NotNull
    public Player opponent(final @NotNull Player player) {
        final DuelTeam playerTeam = teams.val(player.getUniqueId());

        if (playerTeam == null) throw new UnsupportedOperationException();

        final DuelTeam opponentTeam = opponent(playerTeam);

        final UUID opponentUUID = teams.key(opponentTeam);

        for (final Player fighter : players()) {
            if (fighter.getUniqueId().equals(opponentUUID)) return fighter;
        }

        throw new RuntimeException("Couldn't find opponent?");
    }
}
