package dev.continuum.duels.database.impl.stats;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.continuum.duels.ContinuumDuels;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.database.Database;
import dev.continuum.duels.kit.CustomKit;
import dev.continuum.duels.kit.KitContents;
import dev.continuum.duels.stats.PlayerStats;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.library.Utils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.serializers.Serializers;
import dev.manere.utils.sql.query.SQLTableBuilder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLStatsDatabase implements Database<PlayerStats, UUID, PlayerStats> {
    private static final HikariConfig config = new HikariConfig();
    private static HikariDataSource source;
    private static Connection connection;

    @NotNull
    @Override
    public Elements<String> types() {
        return Elements.of(
            "mysql", "sql"
        );
    }

    @NotNull
    @Override
    public Cachable<String, Class<?>> requiredDetails() {
        return Cachable.of(
            Tuple.tuple("ip", String.class),
            Tuple.tuple("port", Integer.class),
            Tuple.tuple("db", String.class),
            Tuple.tuple("user", String.class),
            Tuple.tuple("pass", String.class)
        );
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Boolean> start(final @NotNull Cachable<String, Object> details) {
        return CompletableFuture.supplyAsync(() -> {
            if (!details.hasKey("ip")
                || !details.hasKey("port")
                || !details.hasKey("db")
                || !details.hasKey("user")
                || !details.hasKey("pass")
            ) {
                Utils.plugin().getLogger().severe("Failed to start MySQL database:");
                Utils.plugin().getLogger().severe("- Missing storage configuration details.");
                Utils.plugin().getLogger().severe("  Requires the following:");
                Utils.plugin().getLogger().severe("  - `ip` of type String (IP Address)");
                Utils.plugin().getLogger().severe("  - `port` of type Integer (IP Address Port)");
                Utils.plugin().getLogger().severe("  - `db` of type String (Database Name)");
                Utils.plugin().getLogger().severe("  - `user` of type String (Username)");
                Utils.plugin().getLogger().severe("  - `pass` of type String (Password)");

                Bukkit.getPluginManager().disablePlugin(ContinuumDuels.getPlugin(ContinuumDuels.class));
                return false;
            }

            final Object rawIp = details.val("ip");
            final Object rawPort = details.val("port");
            final Object rawDb = details.val("db");
            final Object rawUser = details.val("user");
            final Object rawPass = details.val("pass");

            if (rawIp == null || rawPort == null || rawDb == null || rawUser == null || rawPass == null) {
                Utils.plugin().getLogger().severe("Failed to start MySQL database:");
                Utils.plugin().getLogger().severe("- Missing storage configuration details.");
                Utils.plugin().getLogger().severe("  Requires the following:");
                Utils.plugin().getLogger().severe("  - `ip` of type String (IP Address)");
                Utils.plugin().getLogger().severe("  - `port` of type Integer (IP Address Port)");
                Utils.plugin().getLogger().severe("  - `db` of type String (Database Name)");
                Utils.plugin().getLogger().severe("  - `user` of type String (Username)");
                Utils.plugin().getLogger().severe("  - `pass` of type String (Password)");

                Bukkit.getPluginManager().disablePlugin(ContinuumDuels.getPlugin(ContinuumDuels.class));
                return false;
            }

            final String ip = (String) requiredDetails().val("ip").cast(rawIp);
            final Integer port = (Integer) requiredDetails().val("port").cast(rawPort);
            final String db = (String) requiredDetails().val("db").cast(rawDb);
            final String user = (String) requiredDetails().val("user").cast(rawUser);
            final String pass = (String) requiredDetails().val("pass").cast(rawPass);

            config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + db);
            config.setUsername(user);
            config.setPassword(pass);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            source = new HikariDataSource(config);

            try {
                connection = source.getConnection();
            } catch (final SQLException e) {
                Utils.plugin().getLogger().severe("Failed to start MySQL database:");
                Utils.plugin().getLogger().severe("- Failed to establish connection");
                Utils.plugin().getLogger().severe("  Please check your storage details, there might be a incorrect value.");

                Bukkit.getPluginManager().disablePlugin(ContinuumDuels.getPlugin(ContinuumDuels.class));
                return false;
            }

            return table();
        });
    }

    public boolean table() {
        final String tableStatement = SQLTableBuilder.of()
            .name("player_statistics")
            .column("owner", "VARCHAR(36) NOT NULL", true)
            .column("kills", "INT NOT NULL")
            .column("deaths", "INT NOT NULL")
            .column("unranked_wins", "INT NOT NULL")
            .column("unranked_losses", "INT NOT NULL")
            .column("ranked_wins", "INT NOT NULL")
            .column("ranked_losses", "INT NOT NULL")
            .build();

        if (source == null) {
            Utils.plugin().getLogger().severe("Failed to start MySQL database:");
            Utils.plugin().getLogger().severe("- Source is null");
            Bukkit.getPluginManager().disablePlugin(ContinuumDuels.getPlugin(ContinuumDuels.class));
            return false;
        }

        try (final PreparedStatement statement = connection.prepareStatement(tableStatement)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            Utils.plugin().getLogger().severe("Failed to start MySQL database:");
            Utils.plugin().getLogger().severe("- Failed to create table");
            Bukkit.getPluginManager().disablePlugin(ContinuumDuels.getPlugin(ContinuumDuels.class));
            return false;
        }

        return true;
    }

    @Override
    @CanIgnoreReturnValue
    public boolean stop() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Boolean> save(final @NotNull PlayerStats value) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO player_statistics (owner, kills, deaths, unranked_wins, unranked_losses, ranked_wins, ranked_losses) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "kills = VALUES(kills), " +
                    "deaths = VALUES(deaths), " +
                    "unranked_wins = VALUES(unranked_wins), " +
                    "unranked_losses = VALUES(unranked_losses), " +
                    "ranked_wins = VALUES(ranked_wins), " +
                    "ranked_losses = VALUES(ranked_losses)"
            )) {
                statement.setString(1, value.uuid().toString());
                statement.setInt(2, value.kills());
                statement.setInt(3, value.deaths());
                statement.setInt(4, value.unrankedWins());
                statement.setInt(5, value.unrankedLosses());
                statement.setInt(6, value.rankedWins());
                statement.setInt(7, value.rankedLosses());

                statement.executeUpdate();
                return true;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<PlayerStats> load(final @NotNull UUID value) {
        return CompletableFuture.supplyAsync(() -> {
            try (final PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM player_statistics WHERE owner = ?"
            )) {
                statement.setString(1, value.toString());
                final ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return PlayerStats.stats(value)
                        .kills(resultSet.getInt("kills"))
                        .deaths(resultSet.getInt("deaths"))
                        .unrankedWins(resultSet.getInt("unranked_wins"))
                        .unrankedLosses(resultSet.getInt("unranked_losses"))
                        .rankedWins(resultSet.getInt("ranked_wins"))
                        .rankedLosses(resultSet.getInt("ranked_losses"));
                } else {
                    return PlayerStats.stats(value);
                }
            } catch (SQLException e) {
                return PlayerStats.stats(value);
            }
        });
    }
}
