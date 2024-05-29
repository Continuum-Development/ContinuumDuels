package dev.continuum.duels.util;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerSpecific {
    @NotNull
    UUID uuid();

    @Nullable
    default OfflinePlayer offlinePlayer() {
        final OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid());
        return player.getName() == null ? null : player;
    }

    @Nullable
    default Player player() {
        final OfflinePlayer offlinePlayer = offlinePlayer();
        return offlinePlayer == null ? null : !offlinePlayer.isOnline() ? null : offlinePlayer instanceof Player player ? player : null;
    }

    @NotNull
    default Audience audience() {
        final Player player = player();
        return player == null ? Audience.empty() : player;
    }
}
