package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public final class IntegerConfigEntry extends ConfigEntryBase<Integer> {
    public IntegerConfigEntry(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull Integer defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    public IntegerConfigEntry(@NotNull String key, @NotNull Integer defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public @NotNull String valueToString() {
        return Value.toString();
    }

    @Override
    public void stringToValue(@NotNull String stringValue) {
        Value = Integer.parseInt(stringValue);
    }
}
