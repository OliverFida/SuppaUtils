package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class CustomSuppaCommand extends CommandBase {
    private final FeatureProviderBase _featureProvider;
    private final LiteralArgumentBuilder<ServerCommandSource> _innerBuilder;

    public CustomSuppaCommand(FeatureProviderBase featureProvider, LiteralArgumentBuilder<ServerCommandSource> innerBuilder) {
        _featureProvider = featureProvider;
        _innerBuilder = innerBuilder;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(getBuilder());
    }

    private LiteralArgumentBuilder<ServerCommandSource> getBuilder() {
        // Feature
        LiteralArgumentBuilder<ServerCommandSource> featureBuilder = literal(_featureProvider.getConfigFeature()).then(_innerBuilder);

        return literal("suppa").then(featureBuilder);
    }
}
