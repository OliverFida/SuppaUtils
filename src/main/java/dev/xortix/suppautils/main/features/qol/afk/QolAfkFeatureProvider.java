package dev.xortix.suppautils.main.features.qol.afk;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.IntegerConfigEntry;
import dev.xortix.suppautils.main.shared.PlayerListManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public final class QolAfkFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "afk";
    }

    @Override
    protected void initImpl() {
        ServerTickEvents.END_WORLD_TICK.register(this::checkAllPlayers);
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> updateLastActive(sender.getUuid()));
        ServerPlayerEvents.JOIN.register(player -> resetTracking(player.getUuid()));
        ServerPlayerEvents.LEAVE.register(player -> resetTracking(player.getUuid()));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "timeout", IntegerArgumentType.integer(10, 3600), "seconds"));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(literal("afk").executes(ctx -> {
            try {
                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
                ServerPlayerEntity player = getPlayer(ctx);

                setAfk(player);
                return Command.SINGLE_SUCCESS;
            } catch (Exception ex) {
                return handleCommandException(ex);
            }
        })));
    }

    @Override
    public void disable() {
        super.disable();

        ArrayList<UUID> uuids = new ArrayList<>(PLAYERS_AFK);
        for (UUID uuid : uuids) {
            resetTracking(uuid);
        }
    }

    private @NotNull IntegerConfigEntry getConfigTimeout() {
        return (IntegerConfigEntry) getConfigEntry("timeout");
    }

    private final Map<UUID, Long> LAST_ACTIVE = new HashMap<>();
    private final Map<UUID, Vec3d> LAST_POSITION = new HashMap<>();
    private final Map<UUID, Vec3d> LAST_ROTATION = new HashMap<>();
    public final ArrayList<UUID> PLAYERS_AFK = new ArrayList<>();

    public void checkAllPlayers(@NotNull ServerWorld world) {
        try {
            if (!getIsEnabled()) return;

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

    public void updateLastActive(@NotNull UUID uuid) {
        try {
            LAST_ACTIVE.put(uuid, System.currentTimeMillis());
        } catch (Exception ignored) {
        }
    }

    public void resetTracking(@NotNull UUID uuid) {
        try {
            LAST_ACTIVE.remove(uuid);
            LAST_POSITION.remove(uuid);
            LAST_ROTATION.remove(uuid);
            PLAYERS_AFK.remove(uuid);
            PlayerListManager.updatePlayerList();
        } catch (Exception ignored) {
        }
    }

    private void setAfk(@NotNull ServerPlayerEntity player) {
        try {
            LAST_ACTIVE.put(player.getUuid(), System.currentTimeMillis() - getConfigTimeout().Value * 1000);
        } catch (Exception ignored) {
        }
    }

    private @NotNull Boolean checkIsAfk(@NotNull UUID uuid, @NotNull Vec3d newPosition, @NotNull Vec3d newRotation) {
        if (checkHasMoved(uuid, newPosition, newRotation)) updateLastActive(uuid);

        long now = System.currentTimeMillis();
        return now - LAST_ACTIVE.get(uuid) >= getConfigTimeout().Value * 1000;
    }

    private @NotNull Boolean checkHasMoved(@NotNull UUID uuid, @NotNull Vec3d newPosition, @NotNull Vec3d newRotation) {
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

    private void setAfk(@NotNull UUID uuid, @NotNull ServerWorld world, @NotNull ServerPlayerEntity player) {
        if (PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.add(uuid);
        world.getServer().getPlayerManager().broadcast(getMessage(player, "ist jetzt AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private void setActive(@NotNull UUID uuid, @NotNull ServerWorld world, @NotNull ServerPlayerEntity player) {
        if (!PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.remove(uuid);
        world.getServer().getPlayerManager().broadcast(getMessage(player, "ist nicht mehr AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private @NotNull MutableText getMessage(@NotNull ServerPlayerEntity player, @NotNull String messageAfterUsername) {
        MutableText message = Text.empty();
        message.append(Text.literal(player.getName().getString() + " " + messageAfterUsername)).formatted(Formatting.GRAY);
        return message;
    }
}
