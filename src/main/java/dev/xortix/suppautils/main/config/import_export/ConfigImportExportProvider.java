package dev.xortix.suppautils.main.config.import_export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.base.ConfigEntryBase;
import dev.xortix.suppautils.main.config.ConfigProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import dev.xortix.suppautils.main.base.CommandBuilderBase;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.CustomSuppaCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public final class ConfigImportExportProvider extends CommandBuilderBase {
    private static boolean isInitializing, isInitialized = false;
    private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("suppautils-config.json");

    public static void init() {
        try {
            if (isInitializing || isInitialized) return;
            isInitializing = true;

            CommandsManager.addToRegistrationList(new CustomSuppaCommand(literal("export")
                    .executes(ctx -> {
                        try {
                            int result = exportConfig();

                            switch (result) {
                                case 200:
                                    ctx.getSource().sendFeedback(() -> Text.literal("§aConfig exported."), false);
                                    break;
                                case 400:
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cConfig export failed."), false);
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        } catch (Exception ex) {
                            return handleCommandException(ex);
                        }
                    })
            ));
            CommandsManager.addToRegistrationList(new CustomSuppaCommand(literal("import")
                    .executes(ctx -> {
                        try {
                            int result = importConfig();

                            switch (result) {
                                case 200:
                                    ctx.getSource().sendFeedback(() -> Text.literal("§aConfig imported."), false);
                                    break;
                                case 400:
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cConfig import failed."), false);
                                    break;
                                case 404:
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cConfig import failed... No file to import."), false);
                                    break;
                            }

                            return Command.SINGLE_SUCCESS;
                        } catch (Exception ex) {
                            return handleCommandException(ex);
                        }
                    })
            ));

            isInitialized = true;
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Initialization of ConfigImportExportProvider failed: " + ex.getMessage());
        } finally {
            isInitializing = false;
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Integer exportConfig() {
        try {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            Map<String, Object> root = new LinkedHashMap<>();

            for (ConfigEntryBase<?> entry : ConfigProvider.Entries.values()) {
                String category = entry.Category();
                String feature = entry.Feature();
                String key = entry.Key;
                String value = entry.valueToString();

                Map<String, Object> categoryMap = (Map<String, Object>) root.computeIfAbsent(category, k -> new LinkedHashMap<>());

                Map<String, Object> featureMap = (Map<String, Object>) categoryMap.computeIfAbsent(feature, k -> new LinkedHashMap<>());

                featureMap.put(key, value);
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
                gson.toJson(root, writer);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Config export failed: " + ex.getMessage());
            return 400;
        }

        return 200;
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Integer importConfig() {
        try {
            Gson gson = new Gson();

            if (!Files.exists(CONFIG_FILE_PATH)) return 404;

            Map<String, Object> root;
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
                root = gson.fromJson(reader, Map.class);
            }

            if (root == null) return 400;

            for (ConfigEntryBase<?> entry : ConfigProvider.Entries.values()) {
                String category = entry.Category();
                String feature = entry.Feature();
                String key = entry.Key;

                Object categoryObj = root.get(category);
                if (!(categoryObj instanceof Map)) continue;
                Map<String, Object> categoryMap = (Map<String, Object>) categoryObj;

                Object featureObj = categoryMap.get(feature);
                if (!(featureObj instanceof Map)) continue;
                Map<String, Object> featureMap = (Map<String, Object>) featureObj;

                Object valueObj = featureMap.get(key);
                if (!(valueObj instanceof String)) continue;
                String value = valueObj.toString();

                entry.stringToValue(value);
                if (key.equals("enabled")) {
                    String featureEnumString = category.toUpperCase() + "_" + feature.toUpperCase();
                    FeaturesManager.FEATURE featureEnum = FeaturesManager.FEATURE.valueOf(featureEnumString);

                    FeatureProviderBase featureProvider = FeaturesManager.Features.get(featureEnum);
                    if (((BooleanConfigEntry) entry).Value) featureProvider.enable();
                    else featureProvider.disable();
                } else {
                    ConfigProvider.updateEntry(entry);
                }
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Config import failed: " + ex.getMessage());
            return 400;
        }

        return 200;
    }
}
