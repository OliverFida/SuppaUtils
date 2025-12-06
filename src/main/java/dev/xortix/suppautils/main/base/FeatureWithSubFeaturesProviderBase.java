package dev.xortix.suppautils.main.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public abstract class FeatureWithSubFeaturesProviderBase extends FeatureProviderBase {
    @Override
    public boolean getIsEnabled() throws Exception {
        throw new Exception("This feature is made of sub-features only. Cannot get if feature is enabled.");
    }

    public boolean getIsEnabled(String _subFeature) {
        BooleanConfigEntry temp = (BooleanConfigEntry) getConfigEntry("enabled_" + _subFeature);
        return temp.Value;
    }

    @Override
    public void enable() throws Exception {
        throw new Exception("This feature is made of sub-features only. Cannot enable feature.");
    }

    public void enable(String subFeature) {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled_" + subFeature);
        configEntry.Value = true;
        ConfigProvider.storeEntry(configEntry);
    }

    @Override
    public void disable() throws Exception {
        throw new Exception("This feature is made of sub-features only. Cannot disable feature.");
    }

    public void disable(String subFeature) {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled_" + subFeature);
        configEntry.Value = false;
        ConfigProvider.storeEntry(configEntry);
    }

    @Override
    protected int checkFeatureEnabledForCommand(CommandContext<ServerCommandSource> ctx) throws Exception {
        throw new Exception("This feature is made of sub-features only. Cannot check if feature is enabled.");
    }

    protected int checkFeatureEnabledForCommand(CommandContext<ServerCommandSource> ctx,  String subFeature) {
        if (!getIsEnabled(subFeature)) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDieses Feature wurde vom Admin deaktiviert."), false);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }
}
