package dev.xortix.suppautils.main.db;

import dev.xortix.suppautils.main.log.Logger;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBProvider {
    private static Connection CONNECTION;
    public static Connection getCONNECTION() {
        return CONNECTION;
    }

    public static void init() {
        try {
            Path dbPath = FabricLoader.getInstance().getConfigDir().resolve("suppautils.db");

            CONNECTION = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
            Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.INFO, "Config Database connected at \"" + dbPath + "\"");

            MigrationProvider.applyInitialMigration();
            MigrationProvider.applyAllMigrations();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }
}
