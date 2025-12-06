package dev.xortix.suppautils.main.shared.commands;

import dev.xortix.suppautils.main.log.Logger;

public abstract class CommandBuilderBase {
    protected int handleCommandException(Exception ex) {
        Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Error during command execution: " + ex.getMessage());
        return 0;
    }
}
