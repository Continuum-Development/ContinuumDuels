package dev.continuum.duels.duel;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.arena.GridPoints;
import dev.continuum.duels.arena.PremadeArena;
import dev.continuum.duels.arena.TemporaryArena;
import dev.continuum.duels.config.ConfigHandler;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.fight.DuelFight;
import dev.continuum.duels.fight.Fights;
import dev.continuum.duels.kit.Kit;
import dev.continuum.duels.util.Collections;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.scheduler.Schedulers;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DuelRequest {
    private static final Set<DuelRequest> requests = Collections.emptySet();

    private final Player sender;
    private final Player target;
    private final int rounds;
    private final Kit kit;
    private final PremadeArena arena;

    public DuelRequest(final @NotNull Player sender, final @NotNull Player target, final int rounds, final @NotNull Kit kit, final @NotNull PremadeArena arena) {
        this.sender = sender;
        this.target = target;
        this.rounds = rounds;
        this.kit = kit;
        this.arena = arena;
    }

    @Nullable
    public static DuelRequest findBySender(final @NotNull Player player) {
        for (final DuelRequest request : requests()) if (request.sender().getUniqueId().equals(player.getUniqueId())) return request;
        return null;
    }

    public void accept() {
        requests.remove(this);

        Messages.message("target_accepted_duel_request", sender, replacements -> {
            replacements.element(Tuple.tuple("sender", sender.getName()));
            replacements.element(Tuple.tuple("target", target.getName()));
            replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
            replacements.element(Tuple.tuple("kit", kit.displayName()));
            replacements.element(Tuple.tuple("arena", arena.info().displayName()));

            return replacements;
        });

        Messages.message("accepted_senders_duel_request", sender, replacements -> {
            replacements.element(Tuple.tuple("sender", sender.getName()));
            replacements.element(Tuple.tuple("target", target.getName()));
            replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
            replacements.element(Tuple.tuple("kit", kit.displayName()));
            replacements.element(Tuple.tuple("arena", arena.info().displayName()));

            return replacements;
        });

        Fights.duel(new DuelFight(kit, TemporaryArena.create(arena, GridPoints.generate()), sender, target, rounds));
    }

    @NotNull
    @CanIgnoreReturnValue
    public static DuelRequest request(final @NotNull Player sender, final @NotNull Player target, final int rounds, final @NotNull Kit kit, final @NotNull PremadeArena arena) {
        final DuelRequest request = new DuelRequest(sender, target, rounds, kit, arena);
        requests.add(request);

        Messages.message("duel_request_sent", sender, replacements -> {
            replacements.element(Tuple.tuple("sender", sender.getName()));
            replacements.element(Tuple.tuple("target", target.getName()));
            replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
            replacements.element(Tuple.tuple("kit", kit.displayName()));
            replacements.element(Tuple.tuple("arena", arena.info().displayName()));

            return replacements;
        });

        Messages.message("duel_request_received", target, replacements -> {
            replacements.element(Tuple.tuple("sender", sender.getName()));
            replacements.element(Tuple.tuple("target", target.getName()));
            replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
            replacements.element(Tuple.tuple("kit", kit.displayName()));
            replacements.element(Tuple.tuple("arena", arena.info().displayName()));

            return replacements;
        });

        Schedulers.sync().execute(() -> {
            if (!requests.contains(request)) return;

            Messages.message("duel_request_expired_sender", sender, replacements -> {
                replacements.element(Tuple.tuple("sender", sender.getName()));
                replacements.element(Tuple.tuple("target", target.getName()));
                replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                replacements.element(Tuple.tuple("kit", kit.displayName()));
                replacements.element(Tuple.tuple("arena", arena.info().displayName()));

                return replacements;
            });

            Messages.message("duel_request_expired_target", target, replacements -> {
                replacements.element(Tuple.tuple("sender", sender.getName()));
                replacements.element(Tuple.tuple("target", target.getName()));
                replacements.element(Tuple.tuple("rounds", String.valueOf(rounds)));
                replacements.element(Tuple.tuple("kit", kit.displayName()));
                replacements.element(Tuple.tuple("arena", arena.info().displayName()));

                return replacements;
            });

            requests.remove(request);
        }, ConfigHandler.value(Integer.class, "duel_request.expire_after_seconds") * 20);

        return request;
    }

    @NotNull
    public static Set<DuelRequest> requests() {
        return requests;
    }

    @NotNull
    public Player sender() {
        return sender;
    }

    @NotNull
    public Player target() {
        return target;
    }

    public int rounds() {
        return rounds;
    }

    @NotNull
    public Kit kit() {
        return kit;
    }

    @NotNull
    public PremadeArena arena() {
        return arena;
    }
}
