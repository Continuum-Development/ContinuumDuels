package dev.continuum.duels.lobby;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.config.ConfigHandler;
import dev.continuum.duels.config.Messages;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.location.LocationUtils;
import dev.manere.utils.misc.ObjectUtils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.scheduler.Schedulers;
import dev.manere.utils.world.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class Lobby {
    private static double cornerOneX = 0.0;
    private static double cornerOneY = 64.0;
    private static double cornerOneZ = 0.0;

    private static double cornerTwoX = 0.0;
    private static double cornerTwoY = 64.0;
    private static double cornerTwoZ = 0.0;

    private static double spawnX = 0.0;
    private static double spawnY = 64.0;
    private static double spawnZ = 0.0;

    private static float spawnPitch = 0.0f;
    private static float spawnYaw = 0.0f;

    private static String world = "world";

    private static final Cachable<LobbyFlag, Boolean> flags = Cachable.of(
        Tuple.tuple(
            LobbyFlag.TELEPORT_ON_JOIN,
            false
        )
    );

    public static void start() {
        Schedulers.async().execute(() -> {
            final FileConfiguration config = ConfigHandler.config();

            world = config.getString("lobby.world", world);

            spawnX = config.getDouble("lobby.spawn.x", spawnX);
            spawnY = config.getDouble("lobby.spawn.y", spawnY);
            spawnZ = config.getDouble("lobby.spawn.z", spawnZ);

            spawnPitch = (float) config.getDouble("lobby.spawn.pitch", spawnPitch);
            spawnYaw = (float) config.getDouble("lobby.spawn.yaw", spawnYaw);

            cornerOneX = config.getDouble("lobby.corners.one.x", cornerOneX);
            cornerOneY = config.getDouble("lobby.corners.one.y", cornerOneY);
            cornerOneZ = config.getDouble("lobby.corners.one.z", cornerOneZ);

            cornerTwoX = config.getDouble("lobby.corners.two.x", cornerTwoX);
            cornerTwoY = config.getDouble("lobby.corners.two.y", cornerTwoY);
            cornerTwoZ = config.getDouble("lobby.corners.two.z", cornerTwoZ);

            final LobbyFlag teleportOnJoin = LobbyFlag.TELEPORT_ON_JOIN;
            flags.cache(teleportOnJoin, config.getBoolean(
                teleportOnJoin.key(), ObjectUtils.defaultIfNull(
                    flags.val(teleportOnJoin),
                    false
                )
            ));
        });
    }

    public static void stop() {
        final FileConfiguration config = ConfigHandler.config();

        config.set("lobby.world", world);

        config.set("lobby.spawn.x", spawnX);
        config.set("lobby.spawn.y", spawnY);
        config.set("lobby.spawn.z", spawnZ);

        config.set("lobby.spawn.pitch", spawnPitch);
        config.set("lobby.spawn.yaw", spawnYaw);

        config.set("lobby.corners.one.x", cornerOneX);
        config.set("lobby.corners.one.y", cornerOneY);
        config.set("lobby.corners.one.z", cornerOneZ);

        config.set("lobby.corners.two.x", cornerTwoX);
        config.set("lobby.corners.two.y", cornerTwoY);
        config.set("lobby.corners.two.z", cornerTwoZ);

        config.set(
            LobbyFlag.TELEPORT_ON_JOIN.key(),
            ObjectUtils.defaultIfNull(
                flags.val(LobbyFlag.TELEPORT_ON_JOIN),
                false
            )
        );
    }

    @NotNull
    public static World world() {
        return ObjectUtils.defaultIfNull(Worlds.world(world), Bukkit.getWorld("world"));
    }

    @NotNull
    public static Location cornerOne() {
        return new Location(world(), cornerOneX, cornerOneY, cornerOneZ);
    }

    @NotNull
    public static Location cornerTwo() {
        return new Location(world(), cornerTwoX, cornerTwoY, cornerTwoZ);
    }

    @NotNull
    public static Location spawn() {
        return new Location(world(), spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
    }

    public static boolean isInLobby(final @NotNull Player player) {
        return isInLobby(player.getLocation());
    }

    public static boolean isInLobby(final @NotNull Location location) {
        return LocationUtils.locIsBetween(cornerOne(), cornerTwo(), location);
    }

    @NotNull
    @CanIgnoreReturnValue
    public static CompletableFuture<Boolean> teleport(final @NotNull Player player) {
        Messages.message("teleporting_to_lobby", player, replacements -> replacements);

        return player.teleportAsync(spawn()).whenCompleteAsync((success, t) -> {
            if (success) Messages.message("teleported_to_lobby_successfully", player, replacements -> replacements);
            else Messages.message("teleported_to_lobby_failed", player, replacements -> replacements);

            player.getOpenInventory().getBottomInventory().clear();
        });
    }
}
