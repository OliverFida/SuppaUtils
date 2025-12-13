package dev.xortix.suppautils.main.helpers;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.config.IntegerConfigEntry;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import dev.xortix.suppautils.main.base.CommandBuilderBase;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class TeleportHelper extends CommandBuilderBase {
    private static boolean isInitializing, isInitialized = false;
    private static final Map<UUID, Long> LAST_TELEPORT = new HashMap<>();
    private static final Map<UUID, Vec3d> LAST_POSITION = new HashMap<>();
    private static final Map<UUID, String> LAST_DIMENSION = new HashMap<>();

    public static void init() {
        try {
            if (isInitializing || isInitialized) return;
            isInitializing = true;

            CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, "tpCountdown", IntegerArgumentType.integer(0, 30), "seconds"));
            CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, "tpCooldown", IntegerArgumentType.integer(0, 3600), "seconds"));
            CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, "tpInterDim", BoolArgumentType.bool(), "enabled"));

            isInitialized = true;
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Initialization of TeleportHelper failed: " + ex.getMessage());
        } finally {
            isInitializing = false;
        }
    }

    public static void teleportPlayer(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        teleportPlayer(ctx, dimension, x, y, z, null, null);
    }

    public static void teleportPlayer(@NotNull ServerPlayerEntity player, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
            teleportPlayer(player, dimension, x, y, z, null, null);
    }

    public static void teleportPlayer(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z, Float pitch, Float yaw) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);
            teleportPlayer(player, dimension, x, y, z, pitch, yaw);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private static void teleportPlayer(@NotNull ServerPlayerEntity player, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z, Float pitch, Float yaw) {
        try {
            int countdownSeconds = getConfigTpCountdown().Value;

            // Check cooldown
            long lastTeleport = LAST_TELEPORT.getOrDefault(player.getUuid(), 0L);
            if (System.currentTimeMillis() - lastTeleport < getConfigTpCooldown().Value * 1000) {
                player.sendMessage(Text.literal("§cDu hast dich erst vor kurzem teleportiert."));
                return;
            }

            // Check interDim
            if (!player.getEntityWorld().getRegistryKey().getValue().toString().equals(dimension) && !getConfigTpInterDim().Value) {
                player.sendMessage(Text.literal("§cDu darfst dich nicht zwischen Dimensionen teleportieren!"));
                return;
            }

            // Check secure
            if (!checkTpSecure(player, dimension, x, y, z)) return;

            Vec3d positionBefore = player.getEntityPos();
            String dimensionBefore = player.getEntityWorld().getRegistryKey().getValue().toString();
            player.sendMessage(Text.literal("§6Teleportationsvorgang startet... Nicht bewegen!"));
            for (int s = 0; s < countdownSeconds; s++) {
                int restSeconds = countdownSeconds - s;
                player.sendMessage(Text.literal("§6" + restSeconds + "..."));
                Thread.sleep(1000);

                Vec3d positionAfter = player.getEntityPos();
                if (!positionBefore.equals(positionAfter)) {
                    player.sendMessage(Text.literal("§cAbbruch... Du hast dich bewegt."));
                    return;
                }
            }

            ServerWorld world = Main.SERVER.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimension)));
            assert world != null;

            Set<PositionFlag> flags = new HashSet<>();
            float tempPitch = player.getPitch();
            float tempYaw = player.getYaw();
            if (pitch != null) tempPitch = pitch;
            if (yaw != null) tempYaw = yaw;

            player.sendMessage(Text.literal("§6Teleportiere..."));
            boolean success = player.teleport(world, x, y, z, flags, tempYaw, tempPitch, false);
            if (!success) {
                player.sendMessage(Text.literal("§cTeleportieren fehlgeschlagen."));
            } else {
                LAST_TELEPORT.put(player.getUuid(), System.currentTimeMillis());
                LAST_POSITION.put(player.getUuid(), positionBefore);
                LAST_DIMENSION.put(player.getUuid(), dimensionBefore);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    public static void teleportPlayerBack(@NotNull CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity player = getPlayer(ctx);

            Vec3d lastPosition = LAST_POSITION.get(player.getUuid());
            String lastDimension = LAST_DIMENSION.get(player.getUuid());
            if (lastPosition == null || lastDimension == null) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cKeine letzte Position bekannt."), false);
                return;
            }

            new Thread(() -> teleportPlayer(ctx, lastDimension, lastPosition.x, lastPosition.y, lastPosition.z)).start();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    public static void teleportPlayerToPlayer(@NotNull ServerPlayerEntity player, @NotNull ServerPlayerEntity target) {
        try {
            Vec3d targetPosition = target.getEntityPos();
            String targetDimension = target.getEntityWorld().getRegistryKey().getValue().toString();

            new Thread(() -> teleportPlayer(player, targetDimension, targetPosition.x, targetPosition.y, targetPosition.z)).start();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    public static void clearChaches() {
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_HOMES).getIsEnabled()) return;
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_SPAWN).getIsEnabled()) return;
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_TPA).getIsEnabled()) return;
        // OFDO: if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_WARPS).getIsEnabled()) return;
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_BACK).getIsEnabled()) return;

        LAST_TELEPORT.clear();
        LAST_POSITION.clear();
        LAST_DIMENSION.clear();
    }

    private static @NotNull Boolean checkTpSecure(@NotNull ServerPlayerEntity player, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        try {
            RegistryKey<World> regKeyDim = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimension));
            ServerWorld world = Main.SERVER.getWorld(regKeyDim);
            if (world == null) throw new Exception();

            // Check BlockUnderneath
            {
                int maxFall = 3;
                for (int i = 1; i <= maxFall + 1; i++) {
                    BlockPos pos = new BlockPos((int)Math.floor(x), (int)Math.floor(y) - i, (int)Math.floor(z));
                    if (!world.getBlockState(pos).isAir() || world.getBlockState(pos).getBlock().equals(Blocks.WATER))
                        break;

                    if (i == maxFall + 1) throw new Exception();
                }
            }

            // Check WillSuffocate
            {
                BlockPos pos = new BlockPos(x.intValue(), y.intValue() + 1, z.intValue());
                if (!world.getBlockState(pos).isAir()) throw new Exception();
            }

            return true;
        } catch (Exception ignored) {
            player.sendMessage(Text.literal("§cAbbruch... Dein Ziel ist nicht sicher."));
            return false;
        }
    }

    private static @NotNull IntegerConfigEntry getConfigTpCountdown() {
        return (IntegerConfigEntry) ConfigProvider.getGlobalConfigEntry("tpCountdown");
    }

    private static @NotNull IntegerConfigEntry getConfigTpCooldown() {
        return (IntegerConfigEntry) ConfigProvider.getGlobalConfigEntry("tpCooldown");
    }

    private static @NotNull BooleanConfigEntry getConfigTpInterDim() {
        return (BooleanConfigEntry) ConfigProvider.getGlobalConfigEntry("tpInterDim");
    }
}
