package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ConfigProvider {
    public static Map<String, ConfigEntry<?>> CONFIG_ENTRIES = new HashMap<>();

    public static void init() {
        try {
            initEntries();

            Statement st = DBProvider.getCONNECTION().createStatement();

            for (ConfigEntry<?> entry : CONFIG_ENTRIES.values()) {
                checkAgainstDB(st, entry);
            }

            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Config loaded successfully");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    public static void storeEntry(ConfigEntry<?> entry) {
        try {
            Statement st = DBProvider.getCONNECTION().createStatement();

            updateValue(st, entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    private static void initEntries() {
        FeatureProviderBase feature;
        ConfigEntry<?> entry;

        // QOL Initials
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_INITIALS);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        CONFIG_ENTRIES.put(entry.Id(), entry);

        // QOL AFK
        feature = FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_AFK);
        entry = new BooleanConfigEntry(feature, "enabled", false);
        CONFIG_ENTRIES.put(entry.Id(), entry);
        entry = new IntegerConfigEntry(feature, "timeout", 300);
        CONFIG_ENTRIES.put(entry.Id(), entry);
    }

    private static void checkAgainstDB(Statement st, ConfigEntry<?> entry) throws SQLException {
        ResultSet result = getValue(st, entry);
        if (!result.next()) {
            insertValue(st, entry);
            return;
        }

        entry.stringToValue(result.getString("value"));
    }

    private static ResultSet getValue(Statement st, ConfigEntry<?> entry) throws SQLException {
        return st.executeQuery("SELECT * FROM \"Config\" WHERE Id = \"" + entry.Id() + "\";");
    }

    private static ResultSet insertValue(Statement st, ConfigEntry<?> entry) throws SQLException {
        st.execute("INSERT INTO Config (Id, Category, Feature, \"Key\", Value) VALUES (\"" + entry.Id() + "\", \"" + entry.Category() + "\", \"" + entry.Feature() + "\", \"" + entry.Key + "\", \"" + entry.valueToString() + "\");");
        return getValue(st, entry);
    }

     private static ResultSet updateValue(Statement st, ConfigEntry<?> entry) throws SQLException {
        st.execute("UPDATE Config SET Value=\"" + entry.valueToString() + "\" WHERE Id=\"" + entry.Id() + "\";");
        return getValue(st, entry);
     }
}
