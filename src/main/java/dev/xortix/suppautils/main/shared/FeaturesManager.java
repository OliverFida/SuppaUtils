package dev.xortix.suppautils.main.shared;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.qol.afk.QolAfkFeatureProvider;

import java.util.HashMap;
import java.util.Map;

public class FeaturesManager {
    public static Map<FEATURE, FeatureProviderBase> Features = new HashMap<FEATURE, FeatureProviderBase>();

    public static void init() {
        FeatureProviderBase feature;

        Features.clear();

//        feature = new QolInitialsFeatureProvider();
//        Features.put(FEATURE.QOL_AFK, feature);
//        feature.init();

        feature = new QolAfkFeatureProvider();
        Features.put(FEATURE.QOL_AFK, feature);
        feature.init();
    }

    public enum FEATURE {
        QOL_INITIALS,
        QOL_AFK
    }
}
