package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ConfigProvider {
    public static Map<String, ConfigEntry> CONFIG_ENTRIES = new HashMap<String, ConfigEntry>();

    public static void init() {
        try {
            initEntries();

            Statement st = DBProvider.getCONNECTION().createStatement();

            for (ConfigEntry entry : CONFIG_ENTRIES.values()) {
                checkAgainstDB(st, entry);
            }

            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Config loaded successfully");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    private static void initEntries() {
        // QOL Initials
        ConfigEntry temp = new BooleanConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_INITIALS, "enabled", false);
        CONFIG_ENTRIES.put(temp.Id(), temp);

        // QOL AFK
        temp = new BooleanConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_AFK, "enabled", false);
        CONFIG_ENTRIES.put(temp.Id(), temp);
        temp = new IntegerConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_AFK, "timeout", 300);
        CONFIG_ENTRIES.put(temp.Id(), temp);
    }

    private static void checkAgainstDB(Statement st, ConfigEntry entry) throws SQLException {
        ResultSet result = getValue(st, entry);
        if (!result.next()) {
            insertValue(st, entry);
            return;
        }

        entry.stringToValue(result.getString("value"));
    }

    private static ResultSet getValue(Statement st, ConfigEntry entry) throws SQLException {
        return st.executeQuery("SELECT * FROM \"Config\" WHERE Id = \"" + entry.Id() + "\";");
    }

    private static ResultSet insertValue(Statement st, ConfigEntry entry) throws SQLException {
        st.execute("INSERT INTO Config (Id, Category, Feature, \"Key\", Value) VALUES (\"" + entry.Id() + "\", \"" + entry.Category() + "\", \"" + entry.Feature() + "\", \"" + entry.Key + "\", \"" + entry.valueToString() + "\");");
        return getValue(st, entry);
    }

    // private static ResultSet updateValue(Statement st, ConfigEntry entry) throws SQLException {
        // OFDO: updateValue
    // }
}
