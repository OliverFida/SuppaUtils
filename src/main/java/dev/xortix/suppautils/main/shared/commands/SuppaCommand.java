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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SuppaCommand extends CommandBase {
    private final TYPE _type;
    private final String _key;
    private final ArgumentType<?> _argumentType;
    private final String _valueDescription;
    private final FeatureProviderBase _featureProvider;

    public SuppaCommand(TYPE type, FeatureProviderBase featureProvider) {
        this(type, featureProvider, "", null, "");
    }

    public SuppaCommand(TYPE type, FeatureProviderBase featureProvider, String key, ArgumentType<?> argumentType, String valueDescription) {
        _type = type;
        _featureProvider = featureProvider;
        if (type == TYPE.CONFIG && (key.isBlank() || argumentType == null || valueDescription.isBlank()))
            throw new IllegalArgumentException("key or valueDescription");
        _key = key;
        _argumentType = argumentType;
        _valueDescription = valueDescription;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
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

    private LiteralArgumentBuilder<ServerCommandSource> getBuilder(Command<ServerCommandSource> executes) {
        return getBuilder(executes, null);
    }

    private LiteralArgumentBuilder<ServerCommandSource> getBuilder(Command<ServerCommandSource> executes, LiteralArgumentBuilder<ServerCommandSource> innerBuilder) {
        // Feature
        LiteralArgumentBuilder<ServerCommandSource> featureBuilder;
        if (innerBuilder == null) {
            featureBuilder = literal(_featureProvider.getConfigFeature()).requires(source -> source.hasPermissionLevel(2)).executes(executes);
        } else {
            featureBuilder = literal(_featureProvider.getConfigFeature()).then(innerBuilder);
        }

        // Type
        LiteralArgumentBuilder<ServerCommandSource> typeBuilder;
        String typeString = switch (_type) {
            case ENABLE -> "enable";
            case DISABLE -> "disable";
            case CONFIG -> "config";
        };
        typeBuilder = literal(typeString).then(featureBuilder);

        // Category
        LiteralArgumentBuilder<ServerCommandSource> categoryBuilder = literal(_featureProvider.getConfigCategory()).then(typeBuilder);

        return literal("suppa").then(categoryBuilder);
    }

    private LiteralArgumentBuilder<ServerCommandSource> getConfigBuilder() {
        // Value
        RequiredArgumentBuilder<ServerCommandSource, ?> valueBuilder = argument(_valueDescription, _argumentType).requires(source -> source.hasPermissionLevel(2)).executes(this::executeConfigFeature);

        // Key
        return literal(_key).then(valueBuilder);
    }

    private int executeEnableFeature(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already enabled."), false);
            return Command.SINGLE_SUCCESS;
        }

        _featureProvider.enable();
        serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been enabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private int executeDisableFeature(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        if (!_featureProvider.getIsEnabled()) {
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already disabled."), false);
            return Command.SINGLE_SUCCESS;
        }

        _featureProvider.disable();
        serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been §cdisabled."), false);

        return Command.SINGLE_SUCCESS;
    }

    private int executeConfigFeature(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        ConfigEntry<?> configEntry = _featureProvider.getConfigEntry(_key);

        // Integer
        if (configEntry instanceof IntegerConfigEntry caseEntry) {
            caseEntry.Value = IntegerArgumentType.getInteger(serverCommandSourceCommandContext, _valueDescription);
            ConfigProvider.storeEntry(caseEntry);
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
            return Command.SINGLE_SUCCESS;
        }

        // Double
        if (configEntry instanceof DoubleConfigEntry caseEntry) {
            caseEntry.Value = DoubleArgumentType.getDouble(serverCommandSourceCommandContext, _valueDescription);
            ConfigProvider.storeEntry(caseEntry);
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
            return Command.SINGLE_SUCCESS;
        }

        // Boolean
        if (configEntry instanceof BooleanConfigEntry caseEntry) {
            caseEntry.Value = BoolArgumentType.getBool(serverCommandSourceCommandContext, _valueDescription);
            ConfigProvider.storeEntry(caseEntry);
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aValue has been set."), false);
            return Command.SINGLE_SUCCESS;
        }

        throw new NotImplementedException();
    }

    public enum TYPE {
        ENABLE, DISABLE, CONFIG
    }
}
