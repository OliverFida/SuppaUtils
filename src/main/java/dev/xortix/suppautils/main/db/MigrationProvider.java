package dev.xortix.suppautils.main.db;

import dev.xortix.suppautils.main.db.migrations.M20251124_2000_Init;
import dev.xortix.suppautils.main.db.migrations.M20251124_2102_CreateTableInitials;
import dev.xortix.suppautils.main.db.migrations.M20251206_0110_AlterTableInitials;
import dev.xortix.suppautils.main.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationProvider {
    private static final Class<? extends MigrationBase> INITIAL_MIGRATION = M20251124_2000_Init.class;
    private static final Class<? extends MigrationBase>[] MIGRATIONS = new Class[] {
            M20251124_2102_CreateTableInitials.class,
            M20251206_0110_AlterTableInitials.class,
    };

    public static void applyInitialMigration() {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            ResultSet result = st.executeQuery("""
                SELECT name FROM sqlite_master WHERE type = 'table' AND name = '_Migrations';
            """);

            if (result.next()) return; // Table _Migrations already exists

            INITIAL_MIGRATION.getDeclaredConstructor().newInstance().apply(st);
            addMigrationToHistory(st, INITIAL_MIGRATION);
            Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.INFO, "Initial migration has been applied");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    public static void applyAllMigrations() {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            boolean migrationsApplied = false;

            for (Class<? extends MigrationBase> migration : MIGRATIONS) {
                if (getMigrationInHistory(st, migration)) continue;

                migration.getDeclaredConstructor().newInstance().apply(st);
                addMigrationToHistory(st, migration);

                migrationsApplied = true;
            }

            if (migrationsApplied)
                Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.INFO, "Migrations have been applied");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.DATABASE, Logger.LogType.CRITICAL, ex.getMessage());
        }
    }

    private static void addMigrationToHistory(Statement st, Class<? extends MigrationBase> migration) throws SQLException {
        String migrationName = migration.getSimpleName();

        st.execute("INSERT INTO \"_Migrations\" (Id) VALUES ('" + migrationName + "');");
    }

    private static boolean getMigrationInHistory(Statement st, Class<? extends MigrationBase> migration) throws SQLException {
        String migrationName = migration.getSimpleName();

        ResultSet result = st.executeQuery("SELECT Id, AppliedAt FROM \"_Migrations\" WHERE Id = \"" + migrationName + "\";");
        return result.next();
    }
}
