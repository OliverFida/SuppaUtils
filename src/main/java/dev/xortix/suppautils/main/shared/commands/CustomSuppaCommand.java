package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.server.command.CommandManager.literal;

public final class CustomSuppaCommand extends CommandBase {
    private final FeatureProviderBase _featureProvider;
    private final LiteralArgumentBuilder<ServerCommandSource> _innerBuilder;

    /// For Feature-Specific commands
    public CustomSuppaCommand(FeatureProviderBase featureProvider, @NotNull LiteralArgumentBuilder<ServerCommandSource> innerBuilder) {
        _featureProvider = featureProvider;
        _innerBuilder = innerBuilder;
    }

    /// For Global commands
    public CustomSuppaCommand(@NotNull LiteralArgumentBuilder<ServerCommandSource> innerBuilder) {
        this(null, innerBuilder);
    }

    @Override
    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess, CommandManager.@NotNull RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(getBuilder());
    }

    private @NotNull LiteralArgumentBuilder<ServerCommandSource> getBuilder() {
        // Feature
        LiteralArgumentBuilder<ServerCommandSource> featureBuilder;
        if (_featureProvider != null) {
            // feature specific
            featureBuilder = literal(_featureProvider.getConfigFeature()).then(_innerBuilder);
        } else {
            // global
            featureBuilder = _innerBuilder;
        }

        return literal("suppa").then(featureBuilder);
    }
}
