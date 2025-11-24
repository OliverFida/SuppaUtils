package dev.xortix.suppautils.main.config;

public class DoubleConfigEntry extends ConfigEntry<Double> {
    public DoubleConfigEntry(CATEGORY category, FEATURE feature, String key, Double defaultValue) {
        super(category, feature, key, defaultValue);
    }

    public DoubleConfigEntry(FEATURE feature, String key, Double defaultValue) {
        super(feature, key, defaultValue);
    }

    @Override
    protected String valueToString() {
        return Value.toString();
    }

    @Override
    protected Double stringToValue(String stringValue) {
        return Double.parseDouble(stringValue);
    }
}
