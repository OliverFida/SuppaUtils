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
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class TeleportHelper {
    private static boolean isInitializing, isInitialized = false;
    private static final Map<UUID, Long> LAST_TELEPORT = new HashMap<>();
    private static final Map<UUID, Vec3> LAST_POSITION = new HashMap<>();
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

    public static void teleportPlayer(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        try {
            ServerPlayer player = ctx.getSource().getPlayer();
            assert player != null;
            int countdownSeconds = getConfigTpCountdown().Value;

            // Check cooldown
            long lastTeleport = LAST_TELEPORT.getOrDefault(player.getUUID(), 0L);
            if (System.currentTimeMillis() - lastTeleport < getConfigTpCooldown().Value * 1000) {
                ctx.getSource().sendSuccess(() -> Component.literal("§cDu hast dich erst vor kurzem teleportiert."), false);
                return;
            }

            // Check interDim
            if (!player.level().dimension().location().toString().equals(dimension) && !getConfigTpInterDim().Value) {
                ctx.getSource().sendSuccess(() -> Component.literal("§cDu darfst dich nicht zwischen Dimensionen teleportieren!"), false);
                return;
            }

            // Check secure
            if (!checkTpSecure(ctx, dimension, x, y, z)) return;

            Vec3 positionBefore = player.position();
            String dimensionBefore = player.level().dimension().location().toString();
            ctx.getSource().sendSuccess(() -> Component.literal("§6Teleportationsvorgang startet... Nicht bewegen!"), false);
            for (int s = 0; s < countdownSeconds; s++) {
                int restSeconds = countdownSeconds - s;
                ctx.getSource().sendSuccess(() -> Component.literal("§6" + restSeconds + "..."), false);
                Thread.sleep(1000);

                Vec3 positionAfter = player.position();
                if (!positionBefore.equals(positionAfter)) {
                    ctx.getSource().sendSuccess(() -> Component.literal("§cAbbruch... Du hast dich bewegt."), false);
                    return;
                }
            }

            ServerLevel world = Main.SERVER.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));
            assert world != null;

            Set<Relative> flags = new HashSet<>();
            ctx.getSource().sendSuccess(() -> Component.literal("§6Teleportiere..."), false);
            boolean success = player.teleportTo(world, x, y, z, flags, player.getYRot(), player.getXRot(), false);
            if (!success) {
                ctx.getSource().sendSuccess(() -> Component.literal("§cTeleportieren fehlgeschlagen."), false);
            } else {
                LAST_TELEPORT.put(player.getUUID(), System.currentTimeMillis());
                LAST_POSITION.put(player.getUUID(), positionBefore);
                LAST_DIMENSION.put(player.getUUID(), dimensionBefore);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    public static void teleportPlayerBack(@NotNull CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayer();
            assert player != null;

            Vec3 lastPosition = LAST_POSITION.get(player.getUUID());
            String lastDimension = LAST_DIMENSION.get(player.getUUID());
            if (lastPosition == null || lastDimension == null) {
                ctx.getSource().sendSuccess(() -> Component.literal("§cKeine letzte Position bekannt."), false);
                return;
            }

            new Thread(() -> teleportPlayer(ctx, lastDimension, lastPosition.x, lastPosition.y, lastPosition.z)).start();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    public static void clearChaches() {
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_HOMES).getIsEnabled()) return;
        // OFDO: if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_SPAWN).getIsEnabled()) return;
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_BACK).getIsEnabled()) return;

        LAST_TELEPORT.clear();
        LAST_POSITION.clear();
        LAST_DIMENSION.clear();
    }

    private static @NotNull Boolean checkTpSecure(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String dimension, @NotNull Double x, @NotNull Double y, @NotNull Double z) {
        try {
            ResourceKey<Level> regKeyDim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
            ServerLevel world = Main.SERVER.getLevel(regKeyDim);
            if (world == null) throw new Exception();

            // Check BlockUnderneath
            {
                int maxFall = 3;
                for (int i = 1; i <= maxFall + 1; i++) {
                    BlockPos pos = new BlockPos(x.intValue(), y.intValue() - i, z.intValue());
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
            ctx.getSource().sendSuccess(() -> Component.literal("§cAbbruch... Dein Ziel ist nicht sicher."), false);
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
