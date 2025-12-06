package dev.xortix.suppautils.main.db.migrations;

import dev.xortix.suppautils.main.db.MigrationBase;

import java.sql.SQLException;
import java.sql.Statement;

public class M20251206_1937_CreateTableHomes extends MigrationBase {
    @Override
    public void apply(Statement st) throws SQLException {
        st.execute("""
            CREATE TABLE QOL_Homes (
                Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                Owner TEXT NOT NULL,
                X NUMERIC NOT NULL,
                Y NUMERIC NOT NULL,
                Z NUMERIC NOT NULL
            );
        """);
    }
}
