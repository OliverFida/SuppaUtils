package dev.xortix.suppautils.main.qol.initials;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class InitialsEntry {
    public final Integer Id;
    public final String Uuid;
    public String Initials;

    public UUID getUuid() {
        return UUID.fromString(Uuid);
    }

    public InitialsEntry(ResultSet rs) throws SQLException {
        Id = rs.getInt("Id");
        Uuid = rs.getString("Uuid");
        Initials = rs.getString("Initials");
    }

    public InitialsEntry(UUID uuid, String initials) {
        this(0, uuid, initials);
    }

    public InitialsEntry(Integer id, UUID uuid, String initials) {
        Id = id;
        Uuid = uuid.toString();
        Initials = initials;
    }
}
