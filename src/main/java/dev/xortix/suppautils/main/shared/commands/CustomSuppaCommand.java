package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.literal;

public final class CustomSuppaCommand extends CommandBase {
    private final FeatureProviderBase _featureProvider;
    private final LiteralArgumentBuilder<CommandSourceStack> _innerBuilder;

    /// For Feature-Specific commands
    public CustomSuppaCommand(FeatureProviderBase featureProvider, @NotNull LiteralArgumentBuilder<CommandSourceStack> innerBuilder) {
        _featureProvider = featureProvider;
        _innerBuilder = innerBuilder;
    }

    /// For Global commands
    public CustomSuppaCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> innerBuilder) {
        this(null, innerBuilder);
    }

    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext registryAccess, @NotNull Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(getBuilder());
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> getBuilder() {
        // Feature
        LiteralArgumentBuilder<CommandSourceStack> featureBuilder;
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
