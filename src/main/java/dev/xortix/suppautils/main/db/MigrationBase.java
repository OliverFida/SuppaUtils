package dev.xortix.suppautils.main.db;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class MigrationBase {
    public MigrationBase() {}

    public abstract void apply(@NotNull Statement st) throws SQLException;
}
