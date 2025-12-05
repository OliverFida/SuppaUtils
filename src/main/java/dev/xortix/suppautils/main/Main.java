package dev.xortix.suppautils.main;

import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Main implements ModInitializer {
    public static final String MOD_ID = "SuppaUtils";
    public static final String INITIALS_CONFIG_URL = "https://unrekt.at/data/qsc-initials.json";

    public static MinecraftServer SERVER;

    @Override
    public void onInitialize() {
        Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.INFO, "Initializing...");

        // DB
        DBProvider.init();

        // SERVER
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);

        // Features
        FeaturesManager.init();

        // Config
        ConfigProvider.init();

        // Commands
//        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> commandDispatcher.register(
//                literal("afk")
//                        .executes(ctx -> {
//                            if (!((BooleanConfigEntry) ConfigProvider.CONFIG_ENTRIES_NEW.get("qol;afk;enabled")).Value) {
//                                // Feature disabled
//
//                                ctx.getSource().sendFeedback(
//                                        () -> Text.literal("§cDieses Feature wurde vom Admin deaktiviert."),
//                                        false
//                                );
//
//                                return 1;
//                            }
//
//                            ServerPlayerEntity player = ctx.getSource().getPlayer();
//                            AfkProvider.setAfk(player);
//
//                            return 1;
//                        })
//        )));
    }
}
