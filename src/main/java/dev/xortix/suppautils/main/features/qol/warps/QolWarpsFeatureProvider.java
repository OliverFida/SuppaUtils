package dev.xortix.suppautils.main.features.qol.warps;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import org.jetbrains.annotations.NotNull;

public class QolWarpsFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "warps";
    }

    @Override
    protected void initImpl() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
    }
}
