package dev.xortix.suppautils.main.shared;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.qol.afk.QolAfkFeatureProvider;
import dev.xortix.suppautils.main.qol.homes.QolHomesFeatureProvider;
import dev.xortix.suppautils.main.qol.initials.QolInitialsFeatureProvider;

import java.util.HashMap;
import java.util.Map;

public class FeaturesManager {
    public static Map<FEATURE, FeatureProviderBase> Features = new HashMap<>();

    public static void init() {
        FeatureProviderBase feature;

        Features.clear();

        feature = new QolInitialsFeatureProvider();
        Features.put(FEATURE.QOL_INITIALS, feature);
        feature.init();

        feature = new QolAfkFeatureProvider();
        Features.put(FEATURE.QOL_AFK, feature);
        feature.init();

        feature = new QolHomesFeatureProvider();
        Features.put(FEATURE.QOL_HOMES, feature);
        feature.init();
    }

    public enum FEATURE {
        QOL_INITIALS,
        QOL_AFK,
        QOL_HOMES,
    }
}
