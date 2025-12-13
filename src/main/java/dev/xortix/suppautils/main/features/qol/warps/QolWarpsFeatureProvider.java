package dev.xortix.suppautils.main.features.qol.warps;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.helpers.TeleportHelper;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class QolWarpsFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "warps";
    }

    @Override
    protected void initImpl() {
        initWarpsFromDb();

        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("warps")
                        .executes(ctx -> {
                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                                    return Command.SINGLE_SUCCESS;

                                List<String> warpNames = new ArrayList<>();
                                Warps.values().forEach(warp -> warpNames.add(warp.Name));

                                String message = "§6Warps: " + String.join(", ", warpNames);
                                ctx.getSource().sendFeedback(() -> Text.literal(message), false);
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("setwarp")
                        .then(argument("name", StringArgumentType.word())
                                .executes(this::handleCommandSetWarp)
                        )
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("delwarp")
                        .then(argument("name", StringArgumentType.word())
                                .executes(this::handleCommandDelWarp)
                        )
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("warp")
                        .then(argument("name", StringArgumentType.word())
                                .executes(this::handleCommandWarp)
                        )
        ));
    }

    @Override
    public void enable() {
        super.enable();

        initWarpsFromDb();
    }

    @Override
    public void disable() {
        super.disable();

        Warps.clear();
    }

    private final String WARPS_TABLE_NAME = "QOL_Warps";
    public final Map<String, WarpEntry> Warps = new HashMap<>();

    private void initWarpsFromDb() {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            Warps.clear();

            ResultSet rs = getEntries(st);
            while (rs.next()) {
                WarpEntry entry = new WarpEntry(rs);
                Warps.put(entry.Id(), entry);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.WARPS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private @NotNull Integer handleCommandSetWarp(@NotNull CommandContext<ServerCommandSource> ctx) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            ServerPlayerEntity player = getPlayer(ctx);
            String name = StringArgumentType.getString(ctx, "name");

            // Check if warp already exists
            WarpEntry existingEntry = Warps.values().stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry != null) {
                // Update Entry
                updateWarp(player, existingEntry);
                ctx.getSource().sendFeedback(() -> Text.literal("§aWarp \"" + name + "\" aktualisiert."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Insert new Entry
            createWarp(player, name);
            ctx.getSource().sendFeedback(() -> Text.literal("§aWarp \"" + name + "\" erstellt."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Integer handleCommandDelWarp(@NotNull CommandContext<ServerCommandSource> ctx) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            String name = StringArgumentType.getString(ctx, "name");

            // Check if warp exists
            WarpEntry existingEntry = Warps.values().stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry == null) {
                // Reject -> warp does not exist
                ctx.getSource().sendFeedback(() -> Text.literal("§cWarp \"" + name + "\" existiert nicht."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Delete Entry
            removeWarp(existingEntry);
            ctx.getSource().sendFeedback(() -> Text.literal("§aWarp \"" + name + "\" §cgelöscht§a."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Integer handleCommandWarp(@NotNull CommandContext<ServerCommandSource> ctx) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            String name = StringArgumentType.getString(ctx, "name");

            // Check if warp exists
            WarpEntry existingEntry = Warps.values().stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry == null) {
                // Reject -> warp does not exist
                ctx.getSource().sendFeedback(() -> Text.literal("§cWarp \"" + name + "\" existiert nicht."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Teleport
            new Thread(() -> TeleportHelper.teleportPlayer(ctx, existingEntry.getDimension(), existingEntry.X, existingEntry.Y, existingEntry.Z)).start();
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private void createWarp(@NotNull ServerPlayerEntity player, @NotNull String name) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            WarpEntry entry = new WarpEntry(name, player.getEntityWorld().getRegistryKey().getValue().toString(), player.getX(), player.getY(), player.getZ());
            ResultSet rs = insertEntry(st, entry);
            entry = new WarpEntry(rs);
            Warps.put(entry.Id(), entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.WARPS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void updateWarp(@NotNull ServerPlayerEntity player, @NotNull WarpEntry entry) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            entry.X = player.getX();
            entry.Y = player.getY();
            entry.Z = player.getZ();
            ResultSet rs = updateEntry(st, entry);
            entry = new WarpEntry(rs);
            Warps.put(entry.Id(), entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.WARPS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void removeWarp(@NotNull WarpEntry entry) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            deleteEntry(st, entry);
            Warps.remove(entry.Id());
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.WARPS, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private @NotNull ResultSet getEntries(@NotNull Statement st) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + WARPS_TABLE_NAME + "';");
    }

    private @NotNull ResultSet getEntry(@NotNull Statement st, @NotNull WarpEntry entry) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + WARPS_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }

    private @NotNull ResultSet updateEntry(@NotNull Statement st, @NotNull WarpEntry entry) throws SQLException {
        st.execute("UPDATE '" + WARPS_TABLE_NAME + "' SET Dimension='" + entry.getDimension() + "',X=" + entry.X + ",Y=" + entry.Y + ",Z=" + entry.Z + " WHERE Id='" + entry.Id() + "';");
        return getEntry(st, entry);
    }

    private @NotNull ResultSet insertEntry(@NotNull Statement st, @NotNull WarpEntry entry) throws SQLException {
        st.execute("INSERT INTO '" + WARPS_TABLE_NAME + "' (Id,Name,Dimension,X,Y,Z) VALUES ('" + entry.Id() + "','" + entry.Name + "','" + entry.getDimension() + "'," + entry.X + "," + entry.Y + "," + entry.Z + ");");
        return getEntry(st, entry);
    }

    private void deleteEntry(@NotNull Statement st, @NotNull WarpEntry entry) throws SQLException {
        st.execute("DELETE FROM '" + WARPS_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }
}
