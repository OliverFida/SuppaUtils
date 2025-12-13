package dev.xortix.suppautils.main.features.qol.warps;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WarpEntry {
    public @NotNull String Id() {
        return Name.toLowerCase();
    }
    public final String Name;
    private Identifier _dimension;
    public @NotNull String getDimension() {
        return _dimension.toString();
    }
    public Double X;
    public Double Y;
    public Double Z;

    public WarpEntry(@NotNull ResultSet rs) throws SQLException {
        Name = rs.getString("Name");
        stringToDimension(rs.getString("Dimension"));
        X = rs.getDouble("X");
        Y = rs.getDouble("Y");
        Z = rs.getDouble("Z");
    }

    public WarpEntry(@NotNull String name, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        Name = name;
        stringToDimension(dimension);
        X = x;
        Y = y;
        Z = z;
    }

    private void stringToDimension(@NotNull String stringValue) {
        String input = stringValue.trim().toLowerCase();

        _dimension = Identifier.tryParse(input);
    }
}
