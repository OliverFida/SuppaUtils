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
    protected @NotNull String valueToString() {
        return Value.toString();
    }

    @Override
    protected void stringToValue(@NotNull String stringValue) {
        Value = Double.parseDouble(stringValue);
    }
}
