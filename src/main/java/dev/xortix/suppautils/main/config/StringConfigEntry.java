package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public final class StringConfigEntry extends ConfigEntryBase<String> {
    public StringConfigEntry(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull String defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    public StringConfigEntry(@NotNull String key, @NotNull String defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public @NotNull String valueToString() {
        return Value;
    }

    @Override
    public void stringToValue(@NotNull String stringValue) {
        Value = stringValue;
    }
}