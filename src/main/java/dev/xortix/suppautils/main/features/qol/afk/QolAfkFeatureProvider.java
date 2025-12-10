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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

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
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> updateLastActive(sender.getUUID()));
        ServerPlayerEvents.JOIN.register(player -> resetTracking(player.getUUID()));
        ServerPlayerEvents.LEAVE.register(player -> resetTracking(player.getUUID()));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "timeout", IntegerArgumentType.integer(10, 3600), "seconds"));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(literal("afk").executes(ctx -> {
            try {
                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;

                ServerPlayer player = ctx.getSource().getPlayer();
                assert player != null;
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
    private final Map<UUID, Vec3> LAST_POSITION = new HashMap<>();
    private final Map<UUID, Vec3> LAST_ROTATION = new HashMap<>();
    public final ArrayList<UUID> PLAYERS_AFK = new ArrayList<>();

    public void checkAllPlayers(@NotNull ServerLevel world) {
        try {
            if (!getIsEnabled()) return;

            for (ServerPlayer player : world.players()) {
                UUID uuid = player.getUUID();
                Vec3 position = player.position();
                Vec3 rotation = player.getLookAngle();

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

    private void setAfk(@NotNull ServerPlayer player) {
        try {
            LAST_ACTIVE.put(player.getUUID(), System.currentTimeMillis() - getConfigTimeout().Value * 1000);
        } catch (Exception ignored) {
        }
    }

    private @NotNull Boolean checkIsAfk(@NotNull UUID uuid, @NotNull Vec3 newPosition, @NotNull Vec3 newRotation) {
        if (checkHasMoved(uuid, newPosition, newRotation)) updateLastActive(uuid);

        long now = System.currentTimeMillis();
        return now - LAST_ACTIVE.get(uuid) >= getConfigTimeout().Value * 1000;
    }

    private @NotNull Boolean checkHasMoved(@NotNull UUID uuid, @NotNull Vec3 newPosition, @NotNull Vec3 newRotation) {
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

    private void setAfk(@NotNull UUID uuid, @NotNull ServerLevel world, @NotNull ServerPlayer player) {
        if (PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.add(uuid);
        world.getServer().getPlayerList().broadcastSystemMessage(getMessage(player, "ist jetzt AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private void setActive(@NotNull UUID uuid, @NotNull ServerLevel world, @NotNull ServerPlayer player) {
        if (!PLAYERS_AFK.contains(uuid)) return;

        PLAYERS_AFK.remove(uuid);
        world.getServer().getPlayerList().broadcastSystemMessage(getMessage(player, "ist nicht mehr AFK."), false);
        PlayerListManager.updatePlayerListEntryForPlayer(player);
    }

    private @NotNull MutableComponent getMessage(@NotNull ServerPlayer player, @NotNull String messageAfterUsername) {
        MutableComponent message = Component.empty();
        message.append(Component.literal(player.getName().getString() + " " + messageAfterUsername)).withStyle(ChatFormatting.GRAY);
        return message;
    }
}
