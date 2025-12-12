package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.ConfigEntryBase;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class ConfigProvider {
    public static Map<String, ConfigEntryBase<?>> Entries = new HashMap<>();
    private static boolean isInitializing, isInitialized = false;

    public static void init() {
        try {
            if (isInitializing || isInitialized) return;
            isInitializing = true;

            initEntries();

            Statement st = DBProvider.getCONNECTION().createStatement();

            for (ConfigEntryBase<?> entry : Entries.values()) {
                checkAgainstDB(st, entry);
            }

            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Config loaded successfully");
            isInitialized = true;
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        } finally {
            isInitializing = false;
        }
    }

    public static void updateEntry(@NotNull ConfigEntryBase<?> entry) {
        try {
            Statement st = DBProvider.getCONNECTION().createStatement();

            updateValue(st, entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    public static @NotNull String getGlobalConfigEntryId(@NotNull String key) {
        return "global;global;" + key;
    }

    public static @NotNull ConfigEntryBase<?> getGlobalConfigEntry(@NotNull String key) {
        return ConfigProvider.Entries.get(getGlobalConfigEntryId(key));
    }

    private static void initEntries() {
        FeatureProviderBase feature;
        ConfigEntryBase<?> entry;

        // Global
        entry = new IntegerConfigEntry("tpCountdown", 5);
        Entries.put(entry.Id(), entry);
        entry = new IntegerConfigEntry("tpCooldown", 60);
        Entries.put(entry.Id(), entry);
        entry = new BooleanConfigEntry("tpInterDim", false);
        Entries.put(entry.Id(), entry);

        // QOL Initials
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_INITIALS);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);

        // QOL AFK
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_AFK);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);
        entry = new IntegerConfigEntry(feature, "timeout", 300);
        Entries.put(entry.Id(), entry);

        // QOL Homes
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_HOMES);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);
        entry = new IntegerConfigEntry(feature, "maxHomes", 3);
        Entries.put(entry.Id(), entry);
        entry = new BooleanConfigEntry(feature, "allowNether", false);
        Entries.put(entry.Id(), entry);
        entry = new BooleanConfigEntry(feature, "allowEnd", false);
        Entries.put(entry.Id(), entry);

        // QOL Spawn
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_SPAWN);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);
        entry = new StringConfigEntry(feature, "spawnpoint_dimension", "");
        Entries.put(entry.Id(), entry);
        entry = new DoubleConfigEntry(feature, "spawnpoint_x", 0D);
        Entries.put(entry.Id(), entry);
        entry = new DoubleConfigEntry(feature, "spawnpoint_y", 0D);
        Entries.put(entry.Id(), entry);
        entry = new DoubleConfigEntry(feature, "spawnpoint_z", 0D);
        Entries.put(entry.Id(), entry);
        entry = new FloatConfigEntry(feature, "spawnpoint_pitch", 0F);
        Entries.put(entry.Id(), entry);
        entry = new FloatConfigEntry(feature, "spawnpoint_yaw", 0F);
        Entries.put(entry.Id(), entry);

        // QOL Warps
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_WARPS);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);

        // QOL TPA
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_TPA);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);

        // QOL Back
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_BACK);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        Entries.put(entry.Id(), entry);
    }

    private static void checkAgainstDB(@NotNull Statement st, @NotNull ConfigEntryBase<?> entry) throws SQLException {
        ResultSet result = getValue(st, entry);
        if (!result.next()) {
            insertValue(st, entry);
            return;
        }

        entry.stringToValue(result.getString("value"));
    }

    private static @NotNull ResultSet getValue(@NotNull Statement st, @NotNull ConfigEntryBase<?> entry) throws SQLException {
        return st.executeQuery("SELECT * FROM \"Config\" WHERE Id = \"" + entry.Id() + "\";");
    }

    private static void insertValue(@NotNull Statement st, @NotNull ConfigEntryBase<?> entry) throws SQLException {
        st.execute("INSERT INTO Config (Id, Category, Feature, \"Key\", Value) VALUES (\"" + entry.Id() + "\", \"" + entry.Category() + "\", \"" + entry.Feature() + "\", \"" + entry.Key + "\", \"" + entry.valueToString() + "\");");
        getValue(st, entry);
    }

    private static void updateValue(@NotNull Statement st, @NotNull ConfigEntryBase<?> entry) throws SQLException {
        st.execute("UPDATE Config SET Value=\"" + entry.valueToString() + "\" WHERE Id=\"" + entry.Id() + "\";");
        getValue(st, entry);
    }
}
