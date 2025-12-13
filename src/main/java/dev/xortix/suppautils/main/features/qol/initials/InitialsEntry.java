package dev.xortix.suppautils.main.features.qol.initials;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class InitialsEntry {
    public final Integer Id;
    public final String Uuid;
    public String Initials;

    public @NotNull UUID getUuid() {
        return UUID.fromString(Uuid);
    }

    public InitialsEntry(@NotNull ResultSet rs) throws SQLException {
        Id = rs.getInt("Id");
        Uuid = rs.getString("Uuid");
        Initials = rs.getString("Initials");
    }

    public InitialsEntry(@NotNull UUID uuid, @NotNull String initials) {
        this(0, uuid, initials);
    }

    private InitialsEntry(@NotNull Integer id, @NotNull UUID uuid, @NotNull String initials) {
        Id = id;
        Uuid = uuid.toString();
        Initials = initials;
    }
}
