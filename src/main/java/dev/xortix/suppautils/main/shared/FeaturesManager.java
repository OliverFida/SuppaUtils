package dev.xortix.suppautils.main.shared;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.features.qol.afk.QolAfkFeatureProvider;
import dev.xortix.suppautils.main.features.qol.back.QolBackFeatureProvider;
import dev.xortix.suppautils.main.features.qol.homes.QolHomesFeatureProvider;
import dev.xortix.suppautils.main.features.qol.initials.QolInitialsFeatureProvider;
import dev.xortix.suppautils.main.features.qol.spawn.QolSpawnFeatureProvider;
import dev.xortix.suppautils.main.features.qol.tpa.QolTpaFeatureProvider;
import dev.xortix.suppautils.main.features.qol.warps.QolWarpsFeatureProvider;

import java.util.HashMap;
import java.util.Map;

public class FeaturesManager {
    public static final Map<FEATURE, FeatureProviderBase> Features = new HashMap<>();

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

        feature = new QolSpawnFeatureProvider();
        Features.put(FEATURE.QOL_SPAWN, feature);
        feature.init();

         feature = new QolWarpsFeatureProvider();
         Features.put(FEATURE.QOL_WARPS, feature);
         feature.init();

         feature = new QolTpaFeatureProvider();
         Features.put(FEATURE.QOL_TPA, feature);
         feature.init();

        feature = new QolBackFeatureProvider();
        Features.put(FEATURE.QOL_BACK, feature);
        feature.init();
    }

    public enum FEATURE {
        QOL_INITIALS,
        QOL_AFK,
        QOL_HOMES,
        QOL_SPAWN,
        QOL_WARPS,
        QOL_TPA,
        QOL_BACK,
    }
}
