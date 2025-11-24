package dev.xortix.suppautils.main.config;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigEntry<T> {
    public String Id() {
        List<String> parts = new ArrayList<String>();

        if (_category != null)
            parts.add(Category());
        parts.add(Feature());
        parts.add(Key);

        return String.join(";", parts);
    }

    private CATEGORY _category = null;
    public String Category() {
        return resolveCategory(_category);
    }

    private final FEATURE _feature;
    public String Feature() {
        return resolveFeature(_feature);
    }

    public final String Key;

    public T Value;

    public ConfigEntry(CATEGORY category, FEATURE feature, String key, T defaultValue) {
        this(feature, key, defaultValue);

        _category = category;
    }

    public ConfigEntry(FEATURE feature, String key, T defaultValue) {
        _feature = feature;
        Key = key;
        Value = defaultValue;
    }

    protected abstract String valueToString();
    protected abstract T stringToValue(String stringValue);

    private String resolveCategory(CATEGORY category) {
        return switch (category) {
            case GLOBAL -> "";
            case QOL -> "qol";
            default -> throw new NotImplementedException("ConfigCategory '" + category.name() + "' not implemented.");
        };
    }

    private String resolveFeature(FEATURE feature) {
        return switch (feature) {
            case QOL_INITIALS -> "initials";
            case QOL_AFK -> "afk";
            default -> throw new NotImplementedException("ConfigFeature '" + feature.name() + "' not implemented.");
        };
    }

    public enum CATEGORY {
        GLOBAL,
        QOL
    }

    public enum FEATURE {
        QOL_INITIALS,
        QOL_AFK
    }
}
