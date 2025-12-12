package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class SuppaCommand extends CommandBase {
    private final TYPE _type;
    private final String _key;
    private final ArgumentType<?> _argumentType;
    private final String _valueDescription;
    private final FeatureProviderBase _featureProvider;

    /// For feature specific ENABLE & DISABLE commands
    public SuppaCommand(@NotNull TYPE type, @NotNull FeatureProviderBase featureProvider) {
        this(type, featureProvider, "", null, "");
    }

    /// For feature specific CONFIG commands
    public SuppaCommand(@NotNull TYPE type, FeatureProviderBase featureProvider, @NotNull String key, ArgumentType<?> argumentType, @NotNull String valueDescription) {
        _type = type;
        _featureProvider = featureProvider;
        if (type == TYPE.CONFIG && (key.isBlank() || argumentType == null || valueDescription.isBlank()))
            throw new IllegalArgumentException("key or valueDescription");
        _key = key;
        _argumentType = argumentType;
        _valueDescription = valueDescription;
    }

    /// For global CONFIG commands
    public SuppaCommand(@NotNull TYPE type, @NotNull String key, ArgumentType<?> argumentType, @NotNull String valueDescription) {
        this(type, null, key, argumentType, valueDescription);
    }

    @Override
    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess, CommandManager.@NotNull RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> builder = switch (_type) {
            case ENABLE -> getBuilder(this::executeEnableFeature);
            case DISABLE -> getBuilder(this::executeDisableFeature);
            case CONFIG -> {
                LiteralArgumentBuilder<ServerCommandSource> configBuilder = getConfigBuilder();
                yield getBuilder(this::executeConfigFeature, configBuilder);
            }
        };

        dispatcher.register(builder);
    }

    private @NotNull LiteralArgumentBuilder<ServerCommandSource> getBuilder(@NotNull Command<ServerCommandSource> executes) {
        return getBuilder(executes, null);
    }

    private @NotNull LiteralArgumentBuilder<ServerCommandSource> getBuilder(@NotNull Command<ServerCommandSource> executes, LiteralArgumentBuilder<ServerCommandSource> innerBuilder) {
        // Feature
        LiteralArgumentBuilder<ServerCommandSource> featureBuilder;
        if (innerBuilder == null) {
            // ENABLE / DISABLE
            featureBuilder = literal(_featureProvider.getConfigFeature()).executes(executes);
        } else if (_featureProvider != null) {
            // Feature specific CONFIG
            featureBuilder = literal(_featureProvider.getConfigFeature()).then(innerBuilder);
        } else {
            // Global CONFIG
            featureBuilder = null;
        }

        // Type
        LiteralArgumentBuilder<ServerCommandSource> typeBuilder;
        String typeString = switch (_type) {
            case ENABLE -> "enable";
            case DISABLE -> "disable";
            case CONFIG -> "config";
        };
        if (featureBuilder != null) {
            // Feature specific
            typeBuilder = literal(typeString).then(featureBuilder);
        } else {
            // Global
            typeBuilder = literal(typeString).then(innerBuilder);
        }

        // Category
        LiteralArgumentBuilder<ServerCommandSource> categoryBuilder;
        if (featureBuilder != null) {
            // Feature specific
            categoryBuilder = literal(_featureProvider.getConfigCategory()).then(typeBuilder);
        } else {
            // Global
            categoryBuilder = literal("global").then(typeBuilder);
        }

        return literal("suppa").requires(source -> source.hasPermissionLevel(2)).then(categoryBuilder);
    }

    private @NotNull LiteralArgumentBuilder<ServerCommandSource> getConfigBuilder() {
        // Value
        RequiredArgumentBuilder<ServerCommandSource, ?> valueBuilder = argument(_valueDescription, _argumentType).requires(source -> source.hasPermissionLevel(2)).executes(this::executeConfigFeature);

        // Key
        return literal(_key).then(valueBuilder);
    }

    private @NotNull Integer executeEnableFeature(@NotNull CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already enabled."), false);
            return 0;
        }

        _featureProvider.enable();
        serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been enabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private @NotNull Integer executeDisableFeature(@NotNull CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (!_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already disabled."), false);
            return 0;
        }

        _featureProvider.disable();
        serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been §cdisabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private @NotNull Integer executeConfigFeature(@NotNull CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        ConfigEntryBase<?> configEntry;
        if (_featureProvider != null) {
            // Feature specific
            configEntry = _featureProvider.getConfigEntry(_key);
        } else {
            configEntry = ConfigProvider.getGlobalConfigEntry(_key);
        }

        // Integer
        switch (configEntry) {
            case IntegerConfigEntry caseEntry -> {
                caseEntry.Value = IntegerArgumentType.getInteger(serverCommandSourceCommandContext, _valueDescription);
                ConfigProvider.updateEntry(caseEntry);
                serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Double
            case DoubleConfigEntry caseEntry -> {
                caseEntry.Value = DoubleArgumentType.getDouble(serverCommandSourceCommandContext, _valueDescription);
                ConfigProvider.updateEntry(caseEntry);
                serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Boolean
            case BooleanConfigEntry caseEntry -> {
                caseEntry.Value = BoolArgumentType.getBool(serverCommandSourceCommandContext, _valueDescription);
                ConfigProvider.updateEntry(caseEntry);
                serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            default -> throw new NotImplementedException();
        }
    }

    public enum TYPE {
        ENABLE, DISABLE, CONFIG
    }
}