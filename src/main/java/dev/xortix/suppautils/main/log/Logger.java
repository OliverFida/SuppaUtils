package dev.xortix.suppautils.main.log;

import dev.xortix.suppautils.main.Main;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public final class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);
    private static final String PREFIX = FabricLoader.getInstance().isDevelopmentEnvironment() ? "" : "[" + Main.MOD_ID + "] ";

    public static void log(@NotNull LogCategory category, @NotNull LogType type, @NotNull String message) {
        String finalMessage = PREFIX;

        switch (category) {
            case GLOBAL:
                break;
            case DATABASE:
                finalMessage += "[Database] ";
                break;
            case INITIALS:
                finalMessage += "[Initials] ";
                break;
            case AFK:
                finalMessage += "[AFK] ";
                break;
            case HOMES:
                finalMessage += "[Homes] ";
                break;
            default:
                throw new NotImplementedException("LogCategory '" + category.name() + "' not implemented.");
        }

        finalMessage += message;

        switch (type) {
            case CRITICAL:
                LOGGER.error(finalMessage);
                if (Main.SERVER != null)
                    Main.SERVER.stop(false);
                // OFDO: Stop server in every possible way
                break;
            case ERROR:
                LOGGER.error(finalMessage);
                break;
            case WARNING:
                LOGGER.warn(finalMessage);
                break;
            case INFO:
                LOGGER.info(finalMessage);
                break;
            case DEBUG:
                LOGGER.debug(finalMessage);
                break;
            default:
                throw new NotImplementedException("LogType '" + type.name() + "' not implemented.");
        }
    }

    public enum LogType {
        CRITICAL,
        ERROR,
        WARNING,
        INFO,
        DEBUG
    }

    public enum LogCategory {
        GLOBAL,
        DATABASE,

        INITIALS,
        AFK,
        HOMES,
        WARPS,
    }
}
