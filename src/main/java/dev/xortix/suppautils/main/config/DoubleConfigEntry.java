package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public final class DoubleConfigEntry extends ConfigEntryBase<Double> {
    public DoubleConfigEntry(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull Double defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    public DoubleConfigEntry(@NotNull String key, @NotNull Double defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public @NotNull String valueToString() {
        return Value.toString();
    }

    @Override
    public void stringToValue(@NotNull String stringValue) {
        Value = Double.parseDouble(stringValue);
    }
}
