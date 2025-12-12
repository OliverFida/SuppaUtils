package dev.xortix.suppautils.main.features.qol.tpa;

import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import org.jetbrains.annotations.NotNull;

public class QolTpaFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "tpa";
    }

    @Override
    protected void initImpl() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
    }
}
