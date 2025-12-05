package dev.xortix.suppautils.main.qol.initials;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.PlayerListManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class QolInitialsFeatureProvider extends FeatureProviderBase {
    @Override
    public String getConfigCategory() {
        return "qol";
    }

    @Override
    public String getConfigFeature() {
        return "initials";
    }

    @Override
    public void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> applyInitials());
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
//        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("add")
//                .then(argument("player", GameProfileArgumentType.gameProfile())
//                        .then(argument("initials", StringArgumentType.word())
//                                .requires(source -> source.hasPermissionLevel(2))
//                                .executes(ctx -> {
//                                    //OFDO
//                                    applyInitials();
//
//                                    return Command.SINGLE_SUCCESS;
//                                })
//                        )
//                )
//        ));
    }

    @Override
    public void enable() {
        super.enable();

        applyInitials();
    }

    @Override
    public void disable() {
        super.disable();

        INITIALS.clear();
        PlayerListManager.updatePlayerList();
    }

    public Map<String, String> INITIALS = Collections.emptyMap();

    public void applyInitials() {
        try {
            if (!getIsEnabled()) return;

            new Thread(this::executeLoadInitials).start();
        } catch (Exception ignored) {
        }
    }

    private void executeLoadInitials() {
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
