package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public final class FloatConfigEntry extends ConfigEntryBase<Float> {
    public FloatConfigEntry(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull Float defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    public FloatConfigEntry(@NotNull String key, @NotNull Float defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public @NotNull String valueToString() {
        return Value.toString();
    }

    @Override
    public void stringToValue(@NotNull String stringValue) {
        Value = Float.parseFloat(stringValue);
    }
}