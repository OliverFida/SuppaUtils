package dev.xortix.suppautils.main.db;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class MigrationBase {
    public MigrationBase() {}

    public abstract void apply(Statement st) throws SQLException;
}
