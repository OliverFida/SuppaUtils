package dev.xortix.suppautils.main.config;

public class IntegerConfigEntry extends ConfigEntry<Integer> {
    public IntegerConfigEntry(CATEGORY category, FEATURE feature, String key, Integer defaultValue) {
        super(category, feature, key, defaultValue);
    }

    public IntegerConfigEntry(FEATURE feature, String key, Integer defaultValue) {
        super(feature, key, defaultValue);
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
