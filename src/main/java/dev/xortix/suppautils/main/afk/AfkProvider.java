package dev.xortix.suppautils.main.afk;

import dev.xortix.suppautils.main.shared.PlayerListManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkProvider {
    private static final long AFK_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
    // private static final long AFK_TIME = 5 * 1000; // DEBUG 10 seconds

    private static final Map<UUID, Long> LAST_ACTIVE = new HashMap<>();
    private static final Map<UUID, Vec3d> LAST_POSITION = new HashMap<>();
    private static final Map<UUID, Vec3d> LAST_ROTATION = new HashMap<>();
    public static final ArrayList<UUID> PLAYERS_AFK = new ArrayList<>();

    public static void checkAllPlayers(ServerWorld world) {
        try {
            for (ServerPlayerEntity player : world.getPlayers()) {
                UUID uuid = player.getUuid();
                Vec3d position = player.getEntityPos();
                Vec3d rotation = player.getRotationVector();

                boolean isAfk = checkIsAfk(uuid, position, rotation);
                if (isAfk) {
                    setAfk(uuid, world, player);
                } else {
                    setActive(uuid, world, player);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void updateLastActive(UUID uuid) {
        try {
            LAST_ACTIVE.put(uuid, System.currentTimeMillis());
        } catch (Exception ignored) {
        }
    }

    public static void setAfk(ServerPlayerEntity player) {
        try {
            LAST_ACTIVE.put(player.getUuid(), System.currentTimeMillis() - AFK_TIME);
        } catch (Exception ignored) {
        }
    }

    private static boolean checkIsAfk(UUID uuid, Vec3d newPosition, Vec3d newRotation) {
        if (checkHasMoved(uuid, newPosition, newRotation)) updateLastActive(uuid);

        long now = System.currentTimeMillis();
        return now - LAST_ACTIVE.get(uuid) >= AFK_TIME;
    }

    private static boolean checkHasMoved(UUID uuid, Vec3d newPosition, Vec3d newRotation) {
        if (LAST_POSITION.containsKey(uuid) && LAST_POSITION.get(uuid).equals(newPosition)) {
            // Position same as before

            if (LAST_ROTATION.containsKey(uuid) && LAST_ROTATION.get(uuid).equals(newRotation)) {
                // Rotation same as before
                return false;
            }
        }

        // Position or Rotation new or different
        LAST_POSITION.put(uuid, newPosition);
        LAST_ROTATION.put(uuid, newRotation);
        return true;
    }

    private static void setAfk(UUID uuid, ServerWorld world, ServerPlayerEntity player) {
        if (PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.add(uuid);
        world.getServer().getPlayerManager().broadcast(getMessage(player, "ist jetzt AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private static void setActive(UUID uuid, ServerWorld world, ServerPlayerEntity player) {
        if (!PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.remove(uuid);
        world.getServer().getPlayerManager().broadcast(getMessage(player, "ist nicht mehr AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private static MutableText getMessage(ServerPlayerEntity player, String messageAfterUsername) {
        MutableText message = Text.empty();
        message.append(Text.literal(player.getName().getString() + " " + messageAfterUsername)).formatted(Formatting.GRAY);
        return message;
    }
}
