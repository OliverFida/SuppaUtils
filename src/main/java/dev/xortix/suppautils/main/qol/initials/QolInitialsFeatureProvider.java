package dev.xortix.suppautils.main.qol.initials;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.PlayerListManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.CustomSuppaCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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
        initFromDb();
        new Thread(this::importFromFile).start();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerListManager.updatePlayerList());
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("set")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("newInitials", StringArgumentType.word())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(ctx -> {
                                    try {
                                        Collection<PlayerConfigEntry> players = GameProfileArgumentType.getProfileArgument(ctx, "player");
                                        if (players.isEmpty()) return Command.SINGLE_SUCCESS;

                                        for (PlayerConfigEntry player : players) {
                                            InitialsEntry currentInitials = Initials.get(player.id());
                                            String newInitials = StringArgumentType.getString(ctx, "newInitials");
                                            updateInitials(player.id(), currentInitials, newInitials);

                                            // New newInitials
                                            if (currentInitials == null)
                                                ctx.getSource().sendFeedback(() -> Text.literal("§aInitials added for player \"" + player.name() + "\"."), false);
                                            // Update newInitials
                                            if (currentInitials != null)
                                                ctx.getSource().sendFeedback(() -> Text.literal("§aInitials updated for player \"" + player.name() + "\"."), false);
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    } catch (Exception ex) {
                                        return handleCommandException(ex);
                                    }
                                })
                        )
                )
        ));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("remove")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            try {
                                Collection<PlayerConfigEntry> players = GameProfileArgumentType.getProfileArgument(ctx, "player");
                                if (players.isEmpty()) return Command.SINGLE_SUCCESS;

                                for (PlayerConfigEntry player : players) {
                                    InitialsEntry currentInitials = Initials.get(player.id());
                                    if (currentInitials == null) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("§cNo initials to delete for player \"" + player.name() + "\"."), false);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    removeInitials(player.id());
                                    ctx.getSource().sendFeedback(() -> Text.literal("§aInitials §cremoved §afor player \"" + player.name() + "\"."), false);
                                }

                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
                )
        ));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("import")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    try {
                        ctx.getSource().sendFeedback(() -> Text.literal("§8Trying to import initials. Please wait..."), false);
                        int result = importFromFile();

                        switch (result) {
                            case 200:
                                ctx.getSource().sendFeedback(() -> Text.literal("§aInitials updated from file."), false);
                                break;
                            case 400:
                                ctx.getSource().sendFeedback(() -> Text.literal("§cImport failed! Please see log."), false);
                                break;
                            case 404:
                                ctx.getSource().sendFeedback(() -> Text.literal("§cNo file found for import."), false);
                                break;
                        }

                        return Command.SINGLE_SUCCESS;
                    } catch (Exception ex) {
                        return handleCommandException(ex);
                    }
                })
        ));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("clear")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    try {
                        List<InitialsEntry> tempEntries = new ArrayList<>(Initials.values());
                        tempEntries.forEach(entry -> removeInitials(entry.getUuid()));

                        ctx.getSource().sendFeedback(() -> Text.literal("§aInitials cleared."), false);
                        return Command.SINGLE_SUCCESS;
                    } catch (Exception ex) {
                        return handleCommandException(ex);
                    }
                })
        ));
    }

    @Override
    public void enable() throws Exception {
        super.enable();

        initFromDb();
        PlayerListManager.updatePlayerList();
    }

    @Override
    public void disable() throws Exception {
        super.disable();

        Initials.clear();
        PlayerListManager.updatePlayerList();
    }

    private final String INITIALS_TABLE_NAME = "QOL_Initials";
    public Map<UUID, InitialsEntry> Initials = new HashMap<>();

    private void initFromDb() {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            Initials.clear();

            ResultSet rs = getEntries(st);
            while (rs.next()) {
                InitialsEntry entry = new InitialsEntry(rs);
                Initials.put(entry.getUuid(), entry);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private int importFromFile() {
        Path filePath = FabricLoader.getInstance().getConfigDir().resolve("suppautils-initials.csv");
        Path filePathAfter = FabricLoader.getInstance().getConfigDir().resolve("suppautils-initials_done.csv");
        if (!Files.exists(filePath)) return 404;

        try {
            List<String> lines = Files.readAllLines(filePath.toAbsolutePath());

            int trys = 0;
            while (Main.SERVER == null && trys < 120) {
                trys++;
                //noinspection BusyWait
                Thread.sleep(1000);
            }
            if (trys == 120) throw new Exception("Server took too long to startup");

            for (String line : lines) {
                try {
                    String[] parts = line.split(";");

                    UUID uuid = getPlayerUuidFromApi(parts[0]);

                    InitialsEntry currentInitials = Initials.get(uuid);
                    updateInitials(uuid, currentInitials, parts[1]);
                } catch (Exception ex) {
                    Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, ex.getMessage());
                }
            }

            Files.move(filePath, filePathAfter, StandardCopyOption.REPLACE_EXISTING);

            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.INFO, "Imported from file");
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, ex.getMessage());
            return 400;
        }

        return 200;
    }

    private UUID getPlayerUuidFromApi(String username) throws Exception {
        try {
            URI apiUri = URI.create("https://api.mojang.com/users/profiles/minecraft/" + username);
            URL apiUrl = apiUri.toURL();

            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) throw new Exception("HTTP error code " + responseCode);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining());
            reader.close();

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            String uuid = json.get("id").getAsString();

            return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"));
        } catch (Exception ex) {
            throw new Exception("Fetching UUID from Mojang API failed for \"" + username + "\": " + ex.getMessage());
        }
    }

    private void updateInitials(UUID uuid, InitialsEntry currentInitials, String newInitials) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            // Local Entry
            InitialsEntry localEntry = currentInitials;
            if (localEntry == null) {
                localEntry = new InitialsEntry(uuid, newInitials);
            }
            localEntry.Initials = newInitials;

            // Db Entry
            ResultSet rs = getEntryByUuid(st, localEntry);
            if (!rs.next()) {
                rs = insertEntry(st, localEntry);
            } else {
                rs = updateEntry(st, rs.getInt("Id"), localEntry);
            }
            InitialsEntry dbEntry = new InitialsEntry(rs);

            Initials.put(dbEntry.getUuid(), dbEntry);

            PlayerListManager.updatePlayerList();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void removeInitials(UUID uuid) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            InitialsEntry localEntry = Initials.get(uuid);
            if (localEntry == null) return;

            deleteEntry(st, localEntry);
            Initials.remove(localEntry.getUuid());

            PlayerListManager.updatePlayerList();
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.INITIALS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private ResultSet getEntries(Statement st) throws SQLException {
        return st.executeQuery("SELECT * FROM \"" + INITIALS_TABLE_NAME + "\";");
    }

    private ResultSet getEntryByUuid(Statement st, InitialsEntry entry) throws SQLException {
        return st.executeQuery("SELECT * FROM \"" + INITIALS_TABLE_NAME + "\" WHERE Uuid = \"" + entry.Uuid + "\";");
    }

    private ResultSet insertEntry(Statement st, InitialsEntry entry) throws SQLException {
        st.execute("INSERT INTO \"" + INITIALS_TABLE_NAME + "\" (Uuid, Initials) VALUES (\"" + entry.Uuid + "\", \"" + entry.Initials + "\");");
        return getEntryByUuid(st, entry);
    }

    private ResultSet updateEntry(Statement st, Integer id, InitialsEntry entry) throws SQLException {
        st.execute("UPDATE \"" + INITIALS_TABLE_NAME + "\" SET Initials=\"" + entry.Initials + "\" WHERE Id=\"" + id + "\";");
        return getEntryByUuid(st, entry);
    }

    private void deleteEntry(Statement st, InitialsEntry entry) throws SQLException {
        st.execute("DELETE FROM\"" + INITIALS_TABLE_NAME + "\" WHERE Id=" + entry.Id + ";");
    }
}
