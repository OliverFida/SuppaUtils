package dev.xortix.suppautils.main.qol.homes;

import net.minecraft.util.Identifier;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeEntry {
    public String Id() {
        return Owner.toLowerCase() + ";" + Name.toLowerCase();
    }
    public final String Owner;
    public final String Name;
    private Identifier _dimension;
    public String getDimension() {
        return _dimension.toString();
    }
    public Double X;
    public Double Y;
    public Double Z;

    public HomeEntry(ResultSet rs) throws SQLException {
        Owner = rs.getString("Owner");
        Name = rs.getString("Name");
        stringToDimension(rs.getString("Dimension"));
        X = rs.getDouble("X");
        Y = rs.getDouble("Y");
        Z = rs.getDouble("Z");
    }

    public HomeEntry(String owner, String name, String dimension, Double x, Double y, Double z) {
        Owner = owner;
        Name = name;
        stringToDimension(dimension);
        X = x;
        Y = y;
        Z = z;
    }

    protected void stringToDimension(String stringValue) {
        String input = stringValue.trim().toLowerCase();

        _dimension = Identifier.tryParse(input);
    }
}
