package dev.xortix.suppautils.main.config;

import dev.xortix.suppautils.main.base.FeatureProviderBase;

public abstract class ConfigEntry<T> {
    private final FeatureProviderBase _featureProvider;

    public String Id() {
        return _featureProvider.getConfigEntryId(Key);
    }

    public String Category() {
        return _featureProvider.getConfigCategory();
    }

    public String Feature() {
        return _featureProvider.getConfigFeature();
    }

    public final String Key;

    public T Value;

    public ConfigEntry(FeatureProviderBase featureProvider, String key, T defaultValue) {
        _featureProvider = featureProvider;

        Key = key;
        Value = defaultValue;
    }

    protected abstract String valueToString();
    protected abstract void stringToValue(String stringValue);
}
