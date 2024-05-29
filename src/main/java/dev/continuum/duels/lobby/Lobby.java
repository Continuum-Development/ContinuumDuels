package dev.continuum.duels.lobby;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.continuum.duels.config.Messages;
import dev.continuum.duels.util.Files;
import dev.continuum.duels.util.Savable;
import dev.manere.utils.library.Utils;
import dev.manere.utils.world.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Lobby implements Savable {
    private static String rawWorld = "world";

    private static double cornerOneX = 0.0;
    private static double cornerOneY = 0.0;
    private static double cornerOneZ = 0.0;

    private static double cornerTwoX = 0.0;
    private static double cornerTwoY = 0.0;
    private static double cornerTwoZ = 0.0;

    private static double spawnX = 0.0;
    private static double spawnY = 64.0;
    private static double spawnZ = 0.0;

    private static float spawnYaw;
    private static float spawnPitch;

    private static boolean teleportOnJoin = false;

    @NotNull
    @CanIgnoreReturnValue
    public static Lobby corner(final @NotNull LobbyCorner corner, final double value) {
        switch (corner) {
            case ONE:
                cornerOneX = value;
                break;
            case TWO:
                cornerTwoX = value;
                break;
        }
        return new Lobby();
    }

    public static double corner(final @NotNull LobbyCorner corner, final @NotNull CornerValue cornerValue) {
        return switch (corner) {
            case ONE -> switch (cornerValue) {
                case X -> cornerOneX;
                case Y -> cornerOneY;
                case Z -> cornerOneZ;
            };
            case TWO -> switch (cornerValue) {
                case X -> cornerTwoX;
                case Y -> cornerTwoY;
                case Z -> cornerTwoZ;
            };
        };
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Lobby spawn(final @NotNull SpawnValue value, final double spawnValue) {
        switch (value) {
            case X -> spawnX = spawnValue;
            case Y -> spawnY = spawnValue;
            case Z -> spawnZ = spawnValue;
            case YAW -> spawnYaw = (float) spawnValue;
            case PITCH -> spawnPitch = (float) spawnValue;
        }
        return new Lobby();
    }
    
    public static double spawn(final @NotNull SpawnValue value) {
        return switch (value) {
            case X -> spawnX;
            case Y -> spawnY;
            case Z -> spawnZ;
            case YAW -> spawnYaw;
            case PITCH -> spawnPitch;
        };
    }

    @NotNull
    public static Location spawn() {
        return new Location(
            Worlds.world(rawWorld()),
            spawn(SpawnValue.X), spawn(SpawnValue.Y), spawn(SpawnValue.Z),
            (float) spawn(SpawnValue.YAW), (float) spawn(SpawnValue.PITCH)
        );
    }

    @NotNull
    public static String rawWorld() {
        return rawWorld;
    }

    @NotNull
    @CanIgnoreReturnValue
    public static Lobby rawWorld(final @NotNull String rawWorld) {
        Lobby.rawWorld = rawWorld;
        return new Lobby();
    }

    public static boolean teleportOnJoin() {
        return teleportOnJoin;
    }

    @NotNull
    public static Lobby lobby() {
        return new Lobby();
    }

    @NotNull
    @CanIgnoreReturnValue
    public Lobby teleportOnJoin(final boolean teleportOnJoin) {
        Lobby.teleportOnJoin = teleportOnJoin;
        return new Lobby();
    }

    @Override
    public boolean save() {
        final File file = Files.create(Files.file("lobby.yml"));
        final FileConfiguration configuration = Files.config(file);

        configuration.set("lobby.world", rawWorld);

        configuration.set("lobby.corners.one.x", cornerOneX);
        configuration.set("lobby.corners.one.y", cornerOneY);
        configuration.set("lobby.corners.one.z", cornerOneZ);

        configuration.set("lobby.corners.two.x", cornerTwoX);
        configuration.set("lobby.corners.two.y", cornerTwoY);
        configuration.set("lobby.corners.two.z", cornerTwoZ);

        configuration.set("lobby.corners.one.x", cornerOneX);
        configuration.set("lobby.corners.one.y", cornerOneY);
        configuration.set("lobby.corners.one.z", cornerOneZ);

        configuration.set("lobby.spawn.x", spawnX);
        configuration.set("lobby.spawn.y", spawnY);
        configuration.set("lobby.spawn.z", spawnZ);

        configuration.set("lobby.spawn.yaw", spawnYaw);
        configuration.set("lobby.spawn.pitch", spawnPitch);

        return Files.saveConfig(file, configuration);
    }

    @Override
    public boolean load() {
        final File file = Files.create(Files.file("lobby.yml"));
        final FileConfiguration configuration = Files.config(file);

        Lobby.rawWorld = configuration.getString("lobby.world", "world");

        Lobby.cornerOneX = configuration.getDouble("lobby.corners.one.x", 0.0);
        Lobby.cornerOneY = configuration.getDouble("lobby.corners.one.y", 64.0);
        Lobby.cornerOneZ = configuration.getDouble("lobby.corners.one.z", 0.0);

        Lobby.cornerTwoX = configuration.getDouble("lobby.corners.two.x", 0.0);
        Lobby.cornerTwoY = configuration.getDouble("lobby.corners.two.y", 64.0);
        Lobby.cornerTwoZ = configuration.getDouble("lobby.corners.two.z", 0.0);

        Lobby.spawnX = configuration.getDouble("lobby.spawn.x", 0.0);
        Lobby.spawnY = configuration.getDouble("lobby.spawn.y", 64.0);
        Lobby.spawnZ = configuration.getDouble("lobby.spawn.z", 0.0);
        Lobby.spawnYaw = (float) configuration.getDouble("lobby.spawn.yaw", 90.0);
        Lobby.spawnPitch = (float) configuration.getDouble("lobby.spawn.pitch", 90.0);

        Lobby.teleportOnJoin = configuration.getBoolean("lobby.settings.teleport_on_join", false);

        return true;
    }

    public static void teleport(final @NotNull Player player) {
        if (spawn().getWorld() == null) {
            Messages.message("lobby.error_occurred", player);
            Utils.plugin().getLogger().severe(
                "Lobby world failed to load... Please edit your lobby settings in lobby.yml! Disabling plugin intentionally..."
            );
            Bukkit.getPluginManager().disablePlugin(Utils.plugin());
            return;
        }

        Messages.message("lobby.teleporting", player);

        player.teleportAsync(spawn()).whenCompleteAsync((success, t) -> {
            if (success) Messages.message("lobby.teleported", player);
            else Messages.message("lobby.error_occurred", player);
            player.getOpenInventory().getBottomInventory().clear();
        });
    }

    public static void teleportQuietly(final @NotNull Player player) {
        player.teleportAsync(spawn()).whenCompleteAsync(
            (success, t) -> player.getOpenInventory().getBottomInventory().clear()
        );
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException();
    }
}
