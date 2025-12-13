package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.ConfigEntryBase;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import org.jetbrains.annotations.NotNull;

public final class BooleanConfigEntry extends ConfigEntryBase<Boolean> {
    public BooleanConfigEntry(@NotNull FeatureProviderBase featureProvider, @NotNull String key, @NotNull Boolean defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    public BooleanConfigEntry(@NotNull String key, @NotNull Boolean defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public @NotNull String valueToString() {
        return Value ? "true" : "false";
    }

    @Override
    public void stringToValue(@NotNull String stringValue) {
        String input = stringValue.trim().toLowerCase();

        Value = input.equals("true");
    }
}
