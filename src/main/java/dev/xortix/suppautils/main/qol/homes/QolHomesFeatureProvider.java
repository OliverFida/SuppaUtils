package dev.xortix.suppautils.main.qol.homes;

import dev.xortix.suppautils.main.base.FeatureWithSubFeaturesProviderBase;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;

public class QolHomesFeatureProvider extends FeatureWithSubFeaturesProviderBase {
    @Override
    public String getConfigCategory() {
        return "qol";
    }

    @Override
    public String getConfigFeature() {
        return "homes";
    }

    @Override
    public void init() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this, "homes"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this, "homes"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this, "spawn"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this, "spawn"));
    }
}
