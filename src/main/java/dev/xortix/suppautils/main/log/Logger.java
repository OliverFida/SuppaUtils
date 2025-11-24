package dev.xortix.suppautils.main.log;

import dev.xortix.suppautils.main.Main;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;

public class Logger {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.MOD_ID);

    public static void log(LogCategory category, LogType type, String message) {
        String finalMessage = "";

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
        AFK
    }
}
