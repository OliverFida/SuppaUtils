package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.resource.featuretoggle.FeatureManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SuppaCommand {
    private final TYPE _type;
    private final String _feature;

    public SuppaCommand(TYPE type, String featureKey) {
        _type = type;
        _feature = featureKey;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        switch(_type) {
            case ENABLE -> registerEnable(dispatcher, registryAccess, registrationEnvironment);
            case DISABLE -> registerDisable(dispatcher, registryAccess, registrationEnvironment);
            case CONFIG -> registerConfig(dispatcher, registryAccess, registrationEnvironment);
        }
    }

    private void registerEnable(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
//        dispatcher.register(literal("suppa")
//                .then(literal("qol")
//                        .then(literal("enable")
//                                .then(literal(_feature)
//                                        .requires(source -> source.hasPermissionLevel(2))
//                                        .executes(ctx -> {
//                                            // OFDOI: /suppa qol enable initials
//                                            FeaturesManager.enableFeature();
//
//                                            return 1;
//                                        })
//                                )
//                        )
//                )
//        );
    }

    private void registerDisable(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

    }

    private void registerConfig(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {

    }

    public enum TYPE {
        ENABLE,
        DISABLE,
        CONFIG
    }
}
