package dev.continuum.duels.game.duel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.game.Game;
import dev.continuum.duels.game.GamePlayer;
import dev.continuum.duels.game.GameTeam;
import dev.continuum.duels.game.Games;
import dev.continuum.duels.kit.premade.PremadeKit;
import dev.continuum.duels.lobby.Lobby;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.library.Utils;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.player.PlayerUtils;
import dev.manere.utils.prettify.ListPrettify;
import dev.manere.utils.scheduler.Schedulers;
import dev.manere.utils.world.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Duel<K> implements Game<K> {
    private final Arena arena;
    private final K kit;
    private final Player one;
    private final Player two;

    private final Cachable<GameTeam, Integer> wins = Cachable.of();
    private final Cachable<GameTeam, Integer> losses = Cachable.of();

    private boolean ended = false;
    private boolean inProgress = false;
    private long duration = 0L;
    private final Set<GameTeam> teams = new HashSet<>();
    private int rounds = 1;
    private int round = 1;
    private BukkitTask task;

    public Duel(final @NotNull Arena arena, final @NotNull K kit, final @NotNull Player one, final @NotNull Player two, final int rounds) {
        this.arena = arena;
        this.kit = kit;

        this.teams.add(GameTeam.team("Red", "<red>", (team, players) -> {
            players.add(GamePlayer.player(one, team));
            return players;
        }));

        this.teams.add(GameTeam.team("Blue", "<blue>", (team, players) -> {
            players.add(GamePlayer.player(two, team));
            return players;
        }));

        this.one = one;
        this.two = two;

        // if (kit instanceof CustomKit customKit) this.rounds = customKit.rounds();
        if (rounds != -1 && rounds != 0) this.rounds = rounds;
    }

    @Override
    public void handleDeath(final @NotNull GamePlayer dead) {
        if (players().size() == 2) {
            GamePlayer winner = dead;

            if (rounds == round) {
                for (final GamePlayer gamePlayer : players()) {
                    if (gamePlayer.uuid().equals(dead.uuid())) continue;
                    winner = gamePlayer;
                }

                for (final GamePlayer player : players()) {
                    Messages.message("player_died", player, placeholders -> {
                        placeholders.element(Tuple.tuple("player", dead.player().getName()));
                        return placeholders;
                    });

                    if (kit instanceof PremadeKit premadeKit) {
                        final GamePlayer finalWinner = winner;
                        Messages.message("duel_ended", player, (placeholders) -> {
                            placeholders.element(Tuple.tuple("winners", finalWinner.player().getName()));
                            placeholders.element(Tuple.tuple("losers", dead.player().getName()));
                            placeholders.element(Tuple.tuple("kit", premadeKit.displayName()));
                            placeholders.element(Tuple.tuple("arena", arena.displayName()));
                            placeholders.element(Tuple.tuple("spectators", ListPrettify.players(spectators())));
                            placeholders.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                            placeholders.element(Tuple.tuple("round", String.valueOf(round)));

                            return placeholders;
                        });
                    }

                    Lobby.teleport(player.player());
                }

                for (final Player spectator : spectators()) {
                    if (!stopSpectating(spectator)) {
                        Bukkit.getLogger().severe("Failed to stop spectating (player = " + spectator.getName() + ")");
                    }
                }

                arena.regenerate();

                teams.clear();
                arena.inUse(false);

                cancelFight();
            } else {
                for (final GamePlayer gamePlayer : players()) {
                    if (gamePlayer.uuid().equals(dead.uuid())) continue;
                    winner = gamePlayer;
                }

                for (final GamePlayer player : players()) {
                    Messages.message("player_died", player, placeholders -> {
                        placeholders.element(Tuple.tuple("player", dead.player().getName()));
                        return placeholders;
                    });

                    if (kit instanceof PremadeKit premadeKit) {
                        final GamePlayer finalWinner = winner;
                        Messages.message("round_ended", player, (placeholders) -> {
                            placeholders.element(Tuple.tuple("winners", finalWinner.player().getName()));
                            placeholders.element(Tuple.tuple("losers", dead.player().getName()));
                            placeholders.element(Tuple.tuple("kit", premadeKit.displayName()));
                            placeholders.element(Tuple.tuple("arena", arena.displayName()));
                            placeholders.element(Tuple.tuple("spectators", ListPrettify.players(spectators())));
                            placeholders.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                            placeholders.element(Tuple.tuple("round", String.valueOf(round)));
                            placeholders.element(Tuple.tuple("next-round", String.valueOf(round + 1)));

                            return placeholders;
                        });
                    }

                    Lobby.teleport(player.player());
                }

                round++;

                losses.cache(dead.team(), ObjectUtils.defaultIfNull(losses.val(dead.team()), 0) + 1);
                wins.cache(opponentTeam(dead.team()), ObjectUtils.defaultIfNull(wins.val(opponentTeam(dead.team())), 0) + 1);

                arena.regenerate();
                startRound();
            }
        } else {
            Bukkit.getLogger().severe("How did blud manage to get more than 2 players into a duel");
        }
    }

    @NotNull
    public GameTeam opponentTeam(final @NotNull GameTeam team) {
        for (final GameTeam gameTeam : teams) if (!gameTeam.equals(team)) return gameTeam;

        return new ArrayList<>(teams()).get(0);
    }

    public void startRound() {
        // TODO

    }

    @NotNull
    private List<Player> spectators() {
        final Cachable<Player, Game<?>> spectatorsTotal = Games.spectators();
        final List<Player> spectators = new ArrayList<>();

        for (final Map.Entry<Player, Game<?>> entry : spectatorsTotal.snapshot().asMap().entrySet()) {
            final Player player = entry.getKey();
            final Game<?> game = entry.getValue();

            if (game.equals(this)) spectators.add(player);
        }

        return spectators;
    }

    @NotNull
    private List<Player> playersAndSpectators() {
        final List<Player> spectators = spectators();
        final Set<? extends GamePlayer> gamePlayers = players();

        final List<Player> players = new ArrayList<>();
        for (final GamePlayer gamePlayer : gamePlayers) players.add(gamePlayer.player());

        return Lists.newArrayList(Iterables.concat(players, spectators));
    }

    public void cancelFight() {
        this.ended = true;
        this.wins.clear();
        this.inProgress = false;

        Games.remove(this);
    }

    @Override
    public void leave(final @NotNull GamePlayer player) {
        if (spectators().contains(player.player())) {
            stopSpectating(player.player());
            return;
        }

        if (players().size() == 2) {
            handleDeath(player);
            return;
        }

        for (final GamePlayer target : players()) Messages.message("left_fight", target,
            elements -> Elements.of(Tuple.tuple("player", player.name()))
        );

        player.player().setGameMode(GameMode.SURVIVAL);
        Lobby.teleport(player.player());
    }

    @Override
    public boolean end() {
        if (task != null) task.cancel();

        for (final Player spectator : spectators()) {
            if (!stopSpectating(spectator)) {
                Bukkit.getLogger().severe("Failed to stop spectating (player = " + spectator.getName() + ")");
            }
        }

        Lobby.teleport(one);
        Lobby.teleport(two);

        arena.regenerate();

        teams.clear();
        arena.inUse(false);

        cancelFight();
        return true;
    }

    @Override
    public boolean start() {
        task = Bukkit.getScheduler().runTaskTimer(Utils.plugin(), () -> duration++, 0L, 1L);

        final Location spawnOne = arena.spawnOne();
        final Location spawnTwo = arena.spawnTwo();

        if (spawnOne == null || spawnTwo == null) return false;

        if (kit instanceof PremadeKit premadeKit) {
            Messages.message("duel_started", one, (placeholders) -> {
                placeholders.element(Tuple.tuple("red", one.getName()));
                placeholders.element(Tuple.tuple("blue", two.getName()));
                placeholders.element(Tuple.tuple("kit", premadeKit.displayName()));
                placeholders.element(Tuple.tuple("arena", arena.displayName()));
                placeholders.element(Tuple.tuple("spectators", ListPrettify.players(spectators())));
                placeholders.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                placeholders.element(Tuple.tuple("round", String.valueOf(round)));

                return placeholders;
            });
        }

        one.teleport(spawnOne);
        two.teleport(spawnTwo);

        arena.inUse(true);

        one.setWalkSpeed(0);
        one.setJumping(false);
        two.setWalkSpeed(0);
        two.setJumping(false);

        one.setHealth(20.0);
        two.setHealth(20.0);

        PlayerUtils.heal(one);
        PlayerUtils.heal(two);

        PlayerUtils.clearPotionEffects(one);
        PlayerUtils.clearPotionEffects(two);

        if (kit instanceof PremadeKit premadeKit) {
            final Cachable<Integer, ItemStack> defaultLayout = ObjectUtils.defaultIfNull(
                premadeKit.defaultLayout(), Cachable.of(Integer.class, ItemStack.class)
            );

            one.getOpenInventory().getBottomInventory().clear();
            two.getOpenInventory().getBottomInventory().clear();

            for (final Map.Entry<Integer, ItemStack> entry : defaultLayout.snapshot().asMap().entrySet()) {
                final int index = entry.getKey();
                final ItemStack item = entry.getValue();

                one.getOpenInventory().getBottomInventory().setItem(index, item);
                two.getOpenInventory().getBottomInventory().setItem(index, item);
            }
        }

        // Todo: ConfigValues.duelStart()
        Schedulers.sync().execute(() -> {
            one.setWalkSpeed(1);
            one.setJumping(true);
            two.setWalkSpeed(1);
            two.setJumping(true);
        }, 0, 100);
        return true;
    }

    @Override
    public boolean spectate(final @NotNull Player spectator, final @Nullable Player target) {
        Games.spectators().cache(spectator, this);

        if (target != null) {
            spectator.teleportAsync(target.getLocation().add(0, 5, 0))
                .thenRun(() -> {
                    for (final Player player : playersAndSpectators()) {
                        Messages.message("started_spectating", player,
                            replacements -> Elements.of(Tuple.tuple("spectator", spectator.getName()))
                        );
                    }

                    spectator.setGameMode(GameMode.SPECTATOR);
                });
        } else {
            final Location center = arena.center();
            if (center == null) return false;

            spectator.teleportAsync(center.add(0, 5, 0))
                .thenRun(() -> {
                    for (final Player player : playersAndSpectators()) {
                        Messages.message("started_spectating", player,
                            replacements -> Elements.of(Tuple.tuple("spectator", spectator.getName()))
                        );
                    }

                    spectator.setGameMode(GameMode.SPECTATOR);
                });
        }

        Schedulers.async().execute(task -> {
            if (!spectators().contains(spectator)) {
                task.cancel();
                return;
            }

            if (ended()) {
                task.cancel();
                return;
            }

            if (!inProgress()) {
                task.cancel();
                return;
            }

            if (!Games.has(this)) {
                task.cancel();
                return;
            }

            if (!Arenas.insideAny(spectator)) {
                final Location center = arena.center();
                if (center == null) return;

                spectator.teleportAsync(center);
            }
        }, 0, 20);
        return true;
    }

    @Override
    public boolean stopSpectating(final @NotNull Player spectator) {
        Games.spectators().del(spectator, this);
        spectator.setGameMode(GameMode.SURVIVAL);

        for (final Player player : playersAndSpectators()) {
            Messages.message("stopped_spectating", player,
                replacements -> Elements.of(Tuple.tuple("spectator", spectator.getName()))
            );
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
        return inProgress;
    }

    @Override
    public boolean friendlyFireAllowed() {
        return false;
    }

    @Override
    public boolean ranked() {
        return false;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public int rounds() {
        return rounds;
    }

    @Override
    public int wins(final @NotNull GameTeam team) {
        final Integer wins = wins().val(team);
        return wins == null || wins == -1 ? 0 : wins;
    }

    @NotNull
    @Override
    public Cachable<GameTeam, Integer> wins() {
        return wins;
    }

    @Override
    public int losses(final @NotNull GameTeam team) {
        final Integer losses = losses().val(team);
        return losses == null || losses == -1 ? 0 : losses;
    }

    @NotNull
    @Override
    public Cachable<GameTeam, Integer> losses() {
        return losses;
    }

    @NotNull
    @Override
    public Tuple<Integer, Integer> winsAndLosses(final @NotNull GameTeam team) {
        final int wins = wins(team);
        final int losses = losses(team);

        return Tuple.tuple(wins, losses);
    }

    @NotNull
    @Override
    public Cachable<GameTeam, Tuple<Integer, Integer>> winsAndLosses() {
        final Cachable<GameTeam, Tuple<Integer, Integer>> winsAndLosses = Cachable.of();

        for (final GameTeam team : teams()) winsAndLosses.cache(team, winsAndLosses(team));

        return winsAndLosses;
    }

    @NotNull
    @Override
    public Arena arena() {
        return arena;
    }

    @NotNull
    @Override
    public K kit() {
        return kit;
    }

    @NotNull
    @Override
    public Set<? extends GameTeam> teams() {
        return teams;
    }
}
