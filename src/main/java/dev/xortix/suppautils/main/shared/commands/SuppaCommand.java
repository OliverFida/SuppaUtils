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
import dev.xortix.suppautils.main.base.FeatureWithSubFeaturesProviderBase;
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
    private final String _subFeature;

    // No Sub-Feature (e.g.: /suppa qol enable afk)
    public SuppaCommand(TYPE type, FeatureProviderBase featureProvider) {
        this(type, featureProvider, "", null, "");
    }

    // With Sub-Feature (e.g.: /suppa qol enable homes)
    public SuppaCommand(TYPE type, FeatureWithSubFeaturesProviderBase featureProvider, String subFeature) {
        this(type, featureProvider, subFeature, "", null, "");
    }

    // No Sub-Feature (e.g.: /suppa qol config afk timeout 300)
    public SuppaCommand(TYPE type, FeatureProviderBase featureProvider, String key, ArgumentType<?> argumentType, String valueDescription) {
        this(type, featureProvider, null, key, argumentType, valueDescription);
    }

    private SuppaCommand(TYPE type, FeatureProviderBase featureProvider, String subFeature, String key, ArgumentType<?> argumentType, String valueDescription) {
        _type = type;
        _featureProvider = featureProvider;
        _subFeature = subFeature;
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
        // Feature / Sub-Feature
        LiteralArgumentBuilder<ServerCommandSource> featureBuilder;

        String feature = _featureProvider.getConfigFeature();
        if (_subFeature != null && (_type == TYPE.ENABLE || _type == TYPE.DISABLE)) feature = _subFeature;

        if (innerBuilder == null) {
            featureBuilder = literal(feature).requires(source -> source.hasPermissionLevel(2)).executes(executes);
        } else {
            featureBuilder = literal(feature).then(innerBuilder);
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
        try {
            if (getIsFeatureEnabled()) {
                serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already enabled."), false);
                return Command.SINGLE_SUCCESS;
            }

            if (_subFeature != null) {
                ((FeatureWithSubFeaturesProviderBase) _featureProvider).enable(_subFeature);
            } else {
                _featureProvider.enable();
            }
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been enabled."), false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private int executeDisableFeature(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        try {
            if (!getIsFeatureEnabled()) {
                serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§cFeature already disabled."), false);
                return Command.SINGLE_SUCCESS;
            }

            if (_subFeature != null) {
                ((FeatureWithSubFeaturesProviderBase) _featureProvider).disable(_subFeature);
            } else {
                _featureProvider.disable();
            }
            serverCommandSourceCommandContext.getSource().sendFeedback(() -> Text.literal("§aFeature has been §cdisabled."), false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private boolean getIsFeatureEnabled() throws Exception {
        if (_subFeature != null) return ((FeatureWithSubFeaturesProviderBase) _featureProvider).getIsEnabled(_subFeature);
        return _featureProvider.getIsEnabled();
    }

    private int executeConfigFeature(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        try {
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
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    public enum TYPE {
        ENABLE, DISABLE, CONFIG
    }
}
