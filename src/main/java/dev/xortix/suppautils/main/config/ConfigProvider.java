package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConfigProvider {
    private static final List<ConfigEntry> CONFIG_ENTRIES = new ArrayList<>() {
        {
            // QOL Initials
            add(new BooleanConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_INITIALS, "enabled", false));

            // QOL AFK
            add(new BooleanConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_AFK, "enabled", false));
            add(new IntegerConfigEntry(ConfigEntry.CATEGORY.QOL, ConfigEntry.FEATURE.QOL_AFK, "timeout", 300));
        }
    };

    public static void init() {
        try {
            Statement st = DBProvider.getCONNECTION().createStatement();

            for (ConfigEntry entry : CONFIG_ENTRIES) {
                checkAgainstDB(st, entry);
            }

            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Config loaded successfully");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.CRITICAL, ex.getMessage());
        }
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
