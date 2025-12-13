package dev.xortix.suppautils.main.db.migrations;

import dev.xortix.suppautils.main.base.MigrationBase;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;

public class M20251213_1317_CreateTableWarps extends MigrationBase {
    @Override
    public void apply(@NotNull Statement st) throws SQLException {
        st.execute("""
            CREATE TABLE QOL_Warps (
                Id TEXT NOT NULL,
                Name TEXT NOT NULL,
                Dimension TEXT NOT NULL,
                X NUMERIC NOT NULL,
                Y NUMERIC NOT NULL,
                Z NUMERIC NOT NULL,
                CONSTRAINT QOL_Homes_PK PRIMARY KEY (Id)
            );
        """);
    }
}
