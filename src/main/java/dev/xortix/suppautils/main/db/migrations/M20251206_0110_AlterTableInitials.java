package dev.xortix.suppautils.main.db.migrations;

import dev.xortix.suppautils.main.db.MigrationBase;

import java.sql.SQLException;
import java.sql.Statement;

public class M20251206_0110_AlterTableInitials extends MigrationBase {
    @Override
    public void apply(Statement st) throws SQLException {
        st.execute("""
            ALTER TABLE QOL_Initials RENAME COLUMN Username TO Uuid;
        """);
    }
}
