package dev.xortix.suppautils.main.base;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.helpers.TeleportHelper;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.chatRequest.ChatRequestManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class FeatureProviderBase extends CommandBuilderBase {
    private boolean isInitializing, isInitialized = false;

    public abstract @NotNull String getConfigCategory();

    public abstract @NotNull String getConfigFeature();

    public final @NotNull String getConfigEntryId(@NotNull String key) {
        return getConfigCategory() + ";" + getConfigFeature() + ";" + key;
    }

    public final @NotNull ConfigEntryBase<?> getConfigEntry(@NotNull String key) {
        return ConfigProvider.Entries.get(getConfigEntryId(key));
    }

    public final boolean getIsEnabled() {
        BooleanConfigEntry temp = (BooleanConfigEntry) getConfigEntry("enabled");
        return temp.Value;
    }

    public final void init() {
        try {
            if (isInitializing || isInitialized) return;
            isInitializing = true;

            initImpl();

            isInitialized = true;
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Initialization of feature \"" + getConfigFeature() + "\" failed: " + ex.getMessage());
        } finally {
            isInitializing = false;
        }
    }

    @MustBeInvokedByOverriders
    protected abstract void initImpl();

    @MustBeInvokedByOverriders
    public void enable() {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled");
        configEntry.Value = true;
        ConfigProvider.updateEntry(configEntry);
    }

    @MustBeInvokedByOverriders
    public void disable() {
        BooleanConfigEntry configEntry = (BooleanConfigEntry) getConfigEntry("enabled");
        configEntry.Value = false;
        ConfigProvider.updateEntry(configEntry);

        TeleportHelper.clearChaches();
        ChatRequestManager.clearChaches();
    }

    protected final int checkFeatureEnabledForCommand(CommandContext<ServerCommandSource> ctx) {
        if (!getIsEnabled()) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDieses Feature wurde vom Admin deaktiviert."), false);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }
}
