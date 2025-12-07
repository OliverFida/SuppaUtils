package dev.xortix.suppautils.main.shared.commands;

import dev.xortix.suppautils.main.log.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilderBase {
    protected @NotNull Integer handleCommandException(@NotNull Exception ex) {
        Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Error during command execution: " + ex.getMessage());
        return 0;
    }
}
