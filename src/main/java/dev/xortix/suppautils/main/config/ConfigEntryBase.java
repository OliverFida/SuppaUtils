package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigEntryBase<T> {
    private final FeatureProviderBase _featureProvider;

    public final @NotNull String Id() {
        if (_featureProvider == null) return ConfigProvider.getGlobalConfigEntryId(Key);
        return _featureProvider.getConfigEntryId(Key);
    }

    public final @NotNull String Category() {
        if (_featureProvider == null) return "global";
        return _featureProvider.getConfigCategory();
    }

    public final @NotNull String Feature() {
        if (_featureProvider == null) return "global";
        return _featureProvider.getConfigFeature();
    }

    public final String Key;

    public T Value;

    /// For feature specific config entries
    public ConfigEntryBase(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull T defaultValue) {
        _featureProvider = featureProvider;

        Key = key;
        Value = defaultValue;
    }

    /// For global config entries
    public ConfigEntryBase(@NotNull String key, @NotNull T defaultValue) {
        _featureProvider = null;
        Key = key;
        Value = defaultValue;
    }

    public abstract @NotNull String valueToString();
    public abstract void stringToValue(@NotNull String stringValue);
}
