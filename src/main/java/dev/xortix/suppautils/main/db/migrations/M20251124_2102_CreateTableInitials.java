package dev.xortix.suppautils.main.db.migrations;

import dev.xortix.suppautils.main.db.MigrationBase;

import java.sql.SQLException;
import java.sql.Statement;

public class M20251124_2102_CreateTableInitials extends MigrationBase {
    @Override
    public void apply(Statement st) throws SQLException {
        st.execute("""
            CREATE TABLE "QOL_Initials" (
                Id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                Username TEXT NOT NULL,
                Initials TEXT(4) NOT NULL,
                UNIQUE (Username, Initials)
            );
        """);
        st.execute("""
            CREATE TABLE "Config" (
                Id TEXT NOT NULL,
                Category TEXT,
                Feature TEXT NOT NULL,
                "Key" TEXT NOT NULL,
                Value TEXT NOT NULL,
                CONSTRAINT Config_PK PRIMARY KEY (Id)
            );
        """);
    }
}
