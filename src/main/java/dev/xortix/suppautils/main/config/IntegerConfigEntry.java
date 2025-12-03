package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;

public class IntegerConfigEntry extends ConfigEntry<Integer> {
    public IntegerConfigEntry(FeatureProviderBase featureProvider, String key, Integer defaultValue) {
        super(featureProvider, key, defaultValue);
    }

    @Override
    protected String valueToString() {
        return Value.toString();
    }

    @Override
    protected Integer stringToValue(String stringValue) {
        return Integer.parseInt(stringValue);
    }
}
