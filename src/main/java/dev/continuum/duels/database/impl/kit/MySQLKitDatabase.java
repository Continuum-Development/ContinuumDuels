package dev.continuum.duels.database.impl.kit;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.continuum.duels.ContinuumDuels;
import dev.continuum.duels.arena.Arena;
import dev.continuum.duels.arena.Arenas;
import dev.continuum.duels.database.Database;
import dev.continuum.duels.kit.CustomKit;
import dev.continuum.duels.kit.KitContents;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.library.Utils;
import dev.manere.utils.model.Tuple;
import dev.manere.utils.serializers.Serializers;
import dev.manere.utils.sql.query.SQLTableBuilder;
import org.bukkit.Bukkit;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MySQLKitDatabase implements Database<CustomKit, CustomKit, Boolean> {
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
            .name("custom_kits")
            .column("owner", "VARCHAR(36) NOT NULL", true)
            .column("name", "TEXT(1000) NOT NULL", true)
            .column("display_name", "TEXT(1000) NOT NULL")
            .column("contents", "TEXT(65535) NOT NULL")
            .column("arena", "TEXT(1000) NOT NULL")
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
    public CompletableFuture<Boolean> save(final @NotNull CustomKit value) {
        return CompletableFuture.supplyAsync(() -> {
            final String insertStatement = "INSERT INTO continuum_kits (owner, name, contents, arena) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE (contents = ?, arena = ?)";

            try (final PreparedStatement statement = connection.prepareStatement(insertStatement)) {
                statement.setString(1, value.uuid().toString());
                statement.setString(2, value.name());
                statement.setString(3, value.displayName()); // Set display_name

                statement.setString(4, Serializers.base64().serializeItemStacks(value.contents().contents().snapshot().asMap()));
                statement.setString(6, Serializers.base64().serializeItemStacks(value.contents().contents().snapshot().asMap()));

                final Arena arena = value.arenas().element(0);
                statement.setString(5, arena == null ? "null" : arena.name());
                statement.setString(7, arena == null ? "null" : arena.name());

                statement.executeUpdate();
            } catch (SQLException e) {
                return false;
            }

            return true;
        });
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @NotNull
    @Override
    @CanIgnoreReturnValue
    public CompletableFuture<Boolean> load(final @NotNull CustomKit value) {
        return CompletableFuture.supplyAsync(() -> {
            final String selectStatement = "SELECT contents, arena FROM continuum_kits WHERE owner = ? AND name = ?";
            ResultSet resultSet = null;

            try (final PreparedStatement statement = connection.prepareStatement(selectStatement)) {
                statement.setString(1, value.uuid().toString());
                statement.setString(2, value.name());

                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    final String contentsBase64 = resultSet.getString("contents");
                    final KitContents kitContents = KitContents.create(Cachable.of(Serializers.base64().deserializeItemStackMap(contentsBase64)));
                    value.contents(kitContents);

                    final String displayName = resultSet.getString("display_name"); // Get display_name
                    value.displayName(displayName); // Set display_name

                    final String arenaName = resultSet.getString("arena");
                    final Elements<Arena> arenas = Elements.of();
                    arenas.element(Arenas.arena(arenaName));
                    value.arenas(arenas);
                    return true;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                Utils.plugin().getLogger().severe("Failed to load data for kit " + value.name());
                Utils.plugin().getLogger().severe("- SQLException:");
                e.printStackTrace();
                return false;
            } finally {
                // Close ResultSet
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        Utils.plugin().getLogger().severe("Failed to load data for kit " + value.name());
                        Utils.plugin().getLogger().severe("- Failed to close ResultSet (cause: database access error):");
                        Utils.plugin().getLogger().severe("- SQLException:");
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
