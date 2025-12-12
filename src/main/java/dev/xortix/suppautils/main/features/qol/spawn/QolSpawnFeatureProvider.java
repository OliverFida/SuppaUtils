package dev.xortix.suppautils.main.features.qol.spawn;

import com.mojang.brigadier.Command;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.config.DoubleConfigEntry;
import dev.xortix.suppautils.main.config.FloatConfigEntry;
import dev.xortix.suppautils.main.config.StringConfigEntry;
import dev.xortix.suppautils.main.helpers.TeleportHelper;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.server.command.CommandManager.literal;

public class QolSpawnFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "spawn";
    }

    @Override
    protected void initImpl() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("setspawn")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            try {
                                ServerPlayerEntity player = getPlayer(ctx);
                                String playerDimension = player.getEntityWorld().getRegistryKey().getValue().toString();
                                Vec3d playerPosition = player.getEntityPos();
                                float playerPitch = player.getPitch();
                                float playerYaw = player.getYaw();

                                StringConfigEntry configDim = getConfigSpawnpointDimension();
                                DoubleConfigEntry configX = getConfigSpawnpointX();
                                DoubleConfigEntry configY = getConfigSpawnpointY();
                                DoubleConfigEntry configZ = getConfigSpawnpointZ();
                                FloatConfigEntry configPitch = getConfigSpawnpointPitch();
                                FloatConfigEntry configYaw = getConfigSpawnpointYaw();

                                configDim.Value = playerDimension;
                                configX.Value = playerPosition.x;
                                configY.Value = playerPosition.y;
                                configZ.Value = playerPosition.z;
                                configPitch.Value = playerPitch;
                                configYaw.Value = playerYaw;

                                ConfigProvider.updateEntry(configDim);
                                ConfigProvider.updateEntry(configX);
                                ConfigProvider.updateEntry(configY);
                                ConfigProvider.updateEntry(configZ);
                                ConfigProvider.updateEntry(configPitch);
                                ConfigProvider.updateEntry(configYaw);

                                ctx.getSource().sendFeedback(() -> Text.literal("§aSpawnpunkt gesetzt."), false);
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("spawn")
                        .executes(ctx -> {
                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;

                                StringConfigEntry configDim = getConfigSpawnpointDimension();
                                DoubleConfigEntry configX = getConfigSpawnpointX();
                                DoubleConfigEntry configY = getConfigSpawnpointY();
                                DoubleConfigEntry configZ = getConfigSpawnpointZ();
                                FloatConfigEntry configPitch = getConfigSpawnpointPitch();
                                FloatConfigEntry configYaw = getConfigSpawnpointYaw();

                                TeleportHelper.teleportPlayer(ctx, configDim.Value, configX.Value, configY.Value, configZ.Value, configPitch.Value, configYaw.Value);

                                ctx.getSource().sendFeedback(() -> Text.literal("§aSpawnpunkt gesetzt."), false);
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
        ));
    }

    private @NotNull StringConfigEntry getConfigSpawnpointDimension() {
        return (StringConfigEntry) getConfigEntry("spawnpoint_dimension");
    }

    private @NotNull DoubleConfigEntry getConfigSpawnpointX() {
        return (DoubleConfigEntry) getConfigEntry("spawnpoint_x");
    }

    private @NotNull DoubleConfigEntry getConfigSpawnpointY() {
        return (DoubleConfigEntry) getConfigEntry("spawnpoint_y");
    }

    private @NotNull DoubleConfigEntry getConfigSpawnpointZ() {
        return (DoubleConfigEntry) getConfigEntry("spawnpoint_z");
    }

    private @NotNull FloatConfigEntry getConfigSpawnpointPitch() {
        return (FloatConfigEntry) getConfigEntry("spawnpoint_pitch");
    }

    private @NotNull FloatConfigEntry getConfigSpawnpointYaw() {
        return (FloatConfigEntry) getConfigEntry("spawnpoint_yaw");
    }
}
