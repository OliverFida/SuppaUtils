package dev.xortix.suppautils.main.db.migrations;

import dev.xortix.suppautils.main.db.MigrationBase;

import java.sql.SQLException;
import java.sql.Statement;

public class M20251124_2000_Init extends MigrationBase {
    @Override
    public void apply(Statement st) throws SQLException {
        st.execute("""
            CREATE TABLE IF NOT EXISTS "_Migrations" (
                Id TEXT NOT NULL,
                AppliedAt TEXT NOT NULL DEFAULT (datetime('now')),
                CONSTRAINT "_Migrations_PK" PRIMARY KEY (Id)
            );
        """);
    }
}
