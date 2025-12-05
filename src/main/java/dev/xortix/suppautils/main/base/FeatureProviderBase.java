package dev.xortix.suppautils.main.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public abstract class FeatureProviderBase {
    public abstract String getConfigCategory();

    public abstract String getConfigFeature();

    public String getConfigEntryId(String key) {
        return getConfigCategory() + ";" + getConfigFeature() + ";" + key;
    }

    public ConfigEntry<?> getConfigEntry(String key) {
        return ConfigProvider.CONFIG_ENTRIES.get(getConfigEntryId(key));
    }

    public boolean getIsEnabled() {
        BooleanConfigEntry temp = (BooleanConfigEntry) getConfigEntry("enabled");
        return temp.Value;
    }

    public abstract void init();

    public void enable() {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled");
        configEntry.Value = true;
        ConfigProvider.storeEntry(configEntry);
    }

    public void disable() {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled");
        configEntry.Value = false;
        ConfigProvider.storeEntry(configEntry);
    }

    protected int checkFeatureEnabledForCommnd(CommandContext<ServerCommandSource> ctx) {
        if (!getIsEnabled()) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDieses Feature wurde vom Admin deaktiviert."), false);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }
}
