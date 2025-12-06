package dev.xortix.suppautils.main.qol.initials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.PlayerListManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.CustomSuppaCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerListManager.updatePlayerList());
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("set")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                        .then(argument("newInitials", StringArgumentType.word())
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(ctx -> {
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
                                })
                        )
                )
        ));
        CommandsManager.addToRegistrationList(new CustomSuppaCommand(this, literal("remove")
                .then(argument("player", GameProfileArgumentType.gameProfile())
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
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
                        })
                )
        ));
    }

    @Override
    public void enable() {
        super.enable();

        initFromDb();
        PlayerListManager.updatePlayerList();
    }

    @Override
    public void disable() {
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
