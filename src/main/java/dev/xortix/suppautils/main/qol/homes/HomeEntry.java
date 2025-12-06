package dev.xortix.suppautils.main.qol.homes;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeEntry {
    public final Integer Id;
    public final String Owner;
    public final Double X;
    public final Double Y;
    public final Double Z;

    public HomeEntry(ResultSet rs) throws SQLException {
        Id = rs.getInt("Id");
        Owner = rs.getString("Owner");
        X = rs.getDouble("X");
        Y = rs.getDouble("Y");
        Z = rs.getDouble("Z");
    }

    public HomeEntry(String owner, Double x, Double y, Double z) {
        this(0, owner, x, y, z);
    }

    private HomeEntry(Integer id, String owner, Double x, Double y, Double z) {
        Id = id;
        Owner = owner;
        X = x;
        Y = y;
        Z = z;
    }
}
