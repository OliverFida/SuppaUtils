package dev.xortix.suppautils.main;

import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.qol.afk.AfkFeatureProvider;

public class FeaturesManager {
    private static final AfkFeatureProvider _afkFeatureProvider = new AfkFeatureProvider();

    public static void initFeatures() {
        _afkFeatureProvider.init();
    }

    public static void enableFeature(FEATURE feature) {
        String configEntryId = getConfigEntryId(feature);
        if (configEntryId == null) return;

        BooleanConfigEntry configEntry = (BooleanConfigEntry)ConfigProvider.CONFIG_ENTRIES.get(configEntryId);
        if (configEntry == null) return;

        configEntry.Value = true;
        ConfigProvider.storeEntry(configEntry);
    }

    private static String getConfigEntryId(FEATURE feature) {
        switch (feature) {
            case QOL_INITIALS:
                return "qol;initials;enabled";
            case QOL_AFK:
                return "qol;afk;enabled";
        }

        return null;
    }

    public enum FEATURE {
        QOL_INITIALS,
        QOL_AFK
    }
}
