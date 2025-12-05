package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;

public class BooleanConfigEntry extends ConfigEntry<Boolean> {
    public BooleanConfigEntry(FeatureProviderBase featureProvider, String key, Boolean defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    @Override
    protected String valueToString() {
        return Value ? "true" : "false";
    }

    @Override
    protected void stringToValue(String stringValue) {
        String input = stringValue.trim().toLowerCase();

        Value = input.equals("true");
    }
}
