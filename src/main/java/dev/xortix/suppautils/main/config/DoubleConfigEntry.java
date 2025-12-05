package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;

public class DoubleConfigEntry extends ConfigEntry<Double> {
    public DoubleConfigEntry(FeatureProviderBase featureProvider, String key, Double defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    @Override
    protected String valueToString() {
        return Value.toString();
    }

    @Override
    protected void stringToValue(String stringValue) {
        Value = Double.parseDouble(stringValue);
    }
}
