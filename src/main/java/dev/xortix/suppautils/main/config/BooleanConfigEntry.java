package dev.xortix.suppautils.main.config;

public class BooleanConfigEntry extends ConfigEntry<Boolean> {
    public BooleanConfigEntry(CATEGORY category, FEATURE feature, String key, Boolean defaultValue) {
        super(category, feature, key, defaultValue);
    }

    public BooleanConfigEntry(FEATURE feature, String key, Boolean defaultValue) {
        super(feature, key, defaultValue);
    }

    @Override
    protected String valueToString() {
        return Value ? "true" : "false";
    }

    @Override
    protected Boolean stringToValue(String stringValue) {
        String input = stringValue.trim().toLowerCase();

        return input.equals("true");
    }
}
