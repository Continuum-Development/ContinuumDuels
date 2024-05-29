package dev.continuum.duels.database.provider;

import dev.continuum.duels.config.ConfigHandler;
import dev.continuum.duels.database.Database;
import dev.continuum.duels.database.DatabaseProvider;
import dev.continuum.duels.database.impl.kit.MySQLKitDatabase;
import dev.continuum.duels.database.impl.kit.YamlKitDatabase;
import dev.continuum.duels.kit.CustomKit;
import dev.manere.utils.cachable.Cachable;
import dev.manere.utils.elements.Elements;
import dev.manere.utils.scheduler.Schedulers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitDatabaseProvider implements DatabaseProvider<CustomKit, CustomKit, Boolean> {
    private static final Database<CustomKit, CustomKit, Boolean> yamlDatabase = new YamlKitDatabase();
    private static final Database<CustomKit, CustomKit, Boolean> mySqlDatabase = new MySQLKitDatabase();

    @NotNull
    @Override
    public Database<CustomKit, CustomKit, Boolean> database() {
        final FileConfiguration config = ConfigHandler.config();
        final ConfigurationSection storage = config.getConfigurationSection("custom_kit_storage");
        if (storage == null) return yamlDatabase;

        for (final String key : storage.getKeys(false)) {
            final ConfigurationSection section = storage.getConfigurationSection(key);
            if (section == null) continue;

            final boolean enabled = section.getBoolean("enabled");
            if (!enabled) continue;

            final String type = section.getString("type");
            if (type == null) continue;

            final Database<CustomKit, CustomKit, Boolean> found = find(type);
            if (found == null) continue;

            return found;
        }

        return yamlDatabase;
    }

    @Nullable
    public Database<CustomKit, CustomKit, Boolean> find(final @NotNull String type) {
        final Elements<String> yamlTypes = yamlDatabase.types();
        if (yamlTypes.has(type.toLowerCase())) return yamlDatabase;

        final Elements<String> mySQLTypes = mySqlDatabase.types();
        if (mySQLTypes.has(type.toLowerCase())) return mySqlDatabase;

        return null;
    }

    @Override
    public void start() {
        Schedulers.async().execute(() -> {
            final FileConfiguration config = ConfigHandler.config();
            final ConfigurationSection storage = config.getConfigurationSection("custom_kit_storage");
            if (storage == null) {
                yamlDatabase.start(Cachable.of());
                return;
            }

            for (final String key : storage.getKeys(false)) {
                final ConfigurationSection section = storage.getConfigurationSection(key);
                if (section == null) continue;

                final ConfigurationSection details = section.getConfigurationSection("details");

                final boolean enabled = section.getBoolean("enabled");
                if (!enabled) continue;

                final String type = section.getString("type");
                if (type == null) continue;

                final Database<CustomKit, CustomKit, Boolean> database = find(type);
                if (database == null) continue;

                final Cachable<String, Class<?>> requiredMap = database.requiredDetails();
                if (requiredMap.snapshot().asMap().isEmpty()) {
                    database.start(Cachable.of());
                    return;
                }

                if (details == null) continue;

                final Cachable<String, Object> provided = Cachable.of();
                for (final String requiredKey : requiredMap.snapshot().asMap().keySet()) {
                    final Class<?> clazzType = requiredMap.val(requiredKey);
                    if (clazzType == null) continue;
                    provided.cache(requiredKey, details.getObject(requiredKey, clazzType));
                }

                database.start(provided);
                return;
            }

            yamlDatabase.start(Cachable.of());
        });
    }

    @Override
    public void stop() {
        database().stop();
    }
}
