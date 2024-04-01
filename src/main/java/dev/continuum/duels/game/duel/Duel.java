package dev.continuum.duels.game.duel;

import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.game.Game;
import dev.continuum.duels.game.GamePlayer;
import dev.continuum.duels.game.GameTeam;
import dev.continuum.duels.game.Games;
import dev.continuum.duels.kit.premade.PremadeKit;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.library.Utils;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.prettify.ListPrettify;
import dev.manere.utils.world.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Duel<K> implements Game<K> {
    private final Arena arena;
    private final K kit;

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

                    player.player().teleport(new Location(Worlds.world("world"), 0, 64, 0));
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

                    player.player().teleport(new Location(Worlds.world("world"), 0, 64, 0));
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
        for (final GameTeam gameTeam : teams) {
            if (!gameTeam.equals(team)) return gameTeam;
        }

        return new ArrayList<>(teams()).get(0);
    }

    public void startRound() {

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

    public void cancelFight() {
        this.ended = true;
        this.wins.clear();
        this.inProgress = false;

        Games.remove(this);
    }

    @Override
    public void leave(final @NotNull GamePlayer player) {
        // TODO: implement this
    }

    @Override
    public boolean end() {
        if (task != null) task.cancel();
        return false;
    }

    @Override
    public boolean start() {
        task = Bukkit.getScheduler().runTaskTimer(Utils.plugin(), () -> duration++, 0L, 1L);
        return false;
    }

    @Override
    public boolean spectate(final @NotNull Player spectator, final @Nullable Player target) {
        // TODO: implement this
        return false;
    }

    @Override
    public boolean stopSpectating(final @NotNull Player spectator) {
        // TODO: implement this
        return false;
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

        for (final GameTeam team : teams()) {
            winsAndLosses.cache(team, winsAndLosses(team));
        }

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
