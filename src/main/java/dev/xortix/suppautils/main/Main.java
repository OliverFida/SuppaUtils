package dev.xortix.suppautils.main;

import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.qol.afk.AfkProvider;
import dev.xortix.suppautils.main.qol.initials.InitialsProvider;
import dev.xortix.suppautils.main.log.Logger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public class Main implements ModInitializer {
    public static final String MOD_ID = "SuppaUtils";
    public static final String INITIALS_CONFIG_URL = "https://unrekt.at/data/qsc-initials.json";

    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Initializing...");

        // DB
        DBProvider.init();

        // Config
        ConfigProvider.init();

        // SERVER
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);

        // Initials
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> InitialsProvider.applyInitials());

        // AFK
        ServerTickEvents.END_WORLD_TICK.register(AfkProvider::checkAllPlayers);
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> AfkProvider.updateLastActive(sender.getUuid()));
        ServerPlayerEvents.JOIN.register(AfkProvider::resetTracking);
        ServerPlayerEvents.LEAVE.register(AfkProvider::resetTracking);

        // Commands
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> commandDispatcher.register(
                literal("afk")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            AfkProvider.setAfk(player);

                            return 1;
                        })
        )));
    }
}
