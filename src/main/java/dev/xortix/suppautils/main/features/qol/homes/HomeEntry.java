package dev.xortix.suppautils.main.features.qol.homes;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.minecraft.resources.ResourceLocation;

public final class HomeEntry {
    public @NotNull String Id() {
        return Owner.toLowerCase() + ";" + Name.toLowerCase();
    }
    public final String Owner;
    public final String Name;
    private ResourceLocation _dimension;
    public @NotNull String getDimension() {
        return _dimension.toString();
    }
    public Double X;
    public Double Y;
    public Double Z;

    public HomeEntry(@NotNull ResultSet rs) throws SQLException {
        Owner = rs.getString("Owner");
        Name = rs.getString("Name");
        stringToDimension(rs.getString("Dimension"));
        X = rs.getDouble("X");
        Y = rs.getDouble("Y");
        Z = rs.getDouble("Z");
    }

    public HomeEntry(@NotNull String owner, @NotNull String name, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        Owner = owner;
        Name = name;
        stringToDimension(dimension);
        X = x;
        Y = y;
        Z = z;
    }

    private void stringToDimension(@NotNull String stringValue) {
        String input = stringValue.trim().toLowerCase();

        _dimension = ResourceLocation.tryParse(input);
    }
}
