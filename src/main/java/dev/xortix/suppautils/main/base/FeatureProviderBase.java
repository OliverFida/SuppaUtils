package dev.xortix.suppautils.main.base;

import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;

public abstract class FeatureProviderBase {
    public abstract String getConfigCategory();
    public abstract String getConfigFeature();
    public String getConfigEntryId(String key) {
        return getConfigCategory() + ";" + getConfigFeature() + ";" + key;
    }
    protected ConfigEntry getConfigEntry(String key) {
        return ConfigProvider.CONFIG_ENTRIES.get(getConfigEntryId(key));
    }
    public boolean getIsEnabled() {
        BooleanConfigEntry temp = (BooleanConfigEntry) getConfigEntry("enabled");
        return temp.Value;
    }

    public abstract void init();
}
