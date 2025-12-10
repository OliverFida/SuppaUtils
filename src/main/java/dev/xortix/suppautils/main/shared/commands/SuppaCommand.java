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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

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
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext registryAccess, @NotNull Commands.CommandSelection registrationEnvironment) {
        LiteralArgumentBuilder<CommandSourceStack> builder = switch (_type) {
            case ENABLE -> getBuilder(this::executeEnableFeature);
            case DISABLE -> getBuilder(this::executeDisableFeature);
            case CONFIG -> {
                LiteralArgumentBuilder<CommandSourceStack> configBuilder = getConfigBuilder();
                yield getBuilder(this::executeConfigFeature, configBuilder);
            }
        };

        dispatcher.register(builder);
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> getBuilder(@NotNull Command<CommandSourceStack> executes) {
        return getBuilder(executes, null);
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> getBuilder(@NotNull Command<CommandSourceStack> executes, LiteralArgumentBuilder<CommandSourceStack> innerBuilder) {
        // Feature
        LiteralArgumentBuilder<CommandSourceStack> featureBuilder;
        if (innerBuilder == null) {
            // ENABLE / DISABLE
            featureBuilder = literal(_featureProvider.getConfigFeature()).requires(source -> source.hasPermission(2)).executes(executes);
        } else if (_featureProvider != null) {
            // Feature specific CONFIG
            featureBuilder = literal(_featureProvider.getConfigFeature()).then(innerBuilder);
        } else {
            // Global CONFIG
            featureBuilder = null;
        }

        // Type
        LiteralArgumentBuilder<CommandSourceStack> typeBuilder;
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
        LiteralArgumentBuilder<CommandSourceStack> categoryBuilder;
        if (featureBuilder != null) {
            // Feature specific
            categoryBuilder = literal(_featureProvider.getConfigCategory()).then(typeBuilder);
        } else {
            // Global
            categoryBuilder = literal("global").then(typeBuilder);
        }

        return literal("suppa").then(categoryBuilder);
    }

    private @NotNull LiteralArgumentBuilder<CommandSourceStack> getConfigBuilder() {
        // Value
        RequiredArgumentBuilder<CommandSourceStack, ?> valueBuilder = argument(_valueDescription, _argumentType).requires(source -> source.hasPermission(2)).executes(this::executeConfigFeature);

        // Key
        return literal(_key).then(valueBuilder);
    }

    private @NotNull Integer executeEnableFeature(@NotNull CommandContext<CommandSourceStack> serverCommandSourceCommandContext) {
        if (_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§cFeature already enabled."), false);
            return 0;
        }

        _featureProvider.enable();
        serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§aFeature has been enabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private @NotNull Integer executeDisableFeature(@NotNull CommandContext<CommandSourceStack> serverCommandSourceCommandContext) {
        if (!_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§cFeature already disabled."), false);
            return 0;
        }

        _featureProvider.disable();
        serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§aFeature has been §cdisabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private @NotNull Integer executeConfigFeature(@NotNull CommandContext<CommandSourceStack> serverCommandSourceCommandContext) {
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
                serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Double
            case DoubleConfigEntry caseEntry -> {
                caseEntry.Value = DoubleArgumentType.getDouble(serverCommandSourceCommandContext, _valueDescription);
                ConfigProvider.updateEntry(caseEntry);
                serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Boolean
            case BooleanConfigEntry caseEntry -> {
                caseEntry.Value = BoolArgumentType.getBool(serverCommandSourceCommandContext, _valueDescription);
                ConfigProvider.updateEntry(caseEntry);
                serverCommandSourceCommandContext.getSource().sendSuccess(() -> Component.literal("§aValue has been set."), false);
                return Command.SINGLE_SUCCESS;
            }

            default -> throw new NotImplementedException();
        }
    }

    public enum TYPE {
        ENABLE, DISABLE, CONFIG
    }
}