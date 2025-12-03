package dev.xortix.suppautils.main.qol.initials;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.PlayerListManager;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class InitialsProvider {
    public static Map<String, String> INITIALS = Collections.emptyMap();

    public static void applyInitials() {
        try {
            if (!((BooleanConfigEntry)ConfigProvider.CONFIG_ENTRIES.get("qol;initials;enabled")).Value) return;

            new Thread(InitialsProvider::executeLoadInitials).start();
        } catch (Exception ignored) {
        }
    }

    private static void executeLoadInitials() {
        try {
            URL url = (new URI(Main.INITIALS_CONFIG_URL)).toURL();

            try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
                INITIALS = new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {
                }.getType());
                Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.INFO, "Loaded " + INITIALS.size() + " initials");
            }
        } catch (Exception e) {
            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, "Failed to load initials: " + e.getMessage());
        }

        PlayerListManager.updatePlayerList();
    }
}
