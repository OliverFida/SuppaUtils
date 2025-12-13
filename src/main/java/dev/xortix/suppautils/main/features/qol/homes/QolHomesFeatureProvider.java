package dev.xortix.suppautils.main.features.qol.homes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.IntegerConfigEntry;
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

public final class QolHomesFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "homes";
    }

    @Override
    protected void initImpl() {
        initHomesFromDb();

        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "maxHomes", IntegerArgumentType.integer(1, 100), "amount"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "allowNether", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "allowEnd", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("homes")
                        .executes(ctx -> {
                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                                    return Command.SINGLE_SUCCESS;
                                ServerPlayerEntity player = getPlayer(ctx);

                                List<HomeEntry> homes = getHomesForPlayer(player);
                                List<String> homeNames = new ArrayList<>();
                                homes.forEach(home -> homeNames.add(home.Name));

                                String message = "§6Deine Homes: " + String.join(", ", homeNames);
                                ctx.getSource().sendFeedback(() -> Text.literal(message), false);
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("sethome")
                        .executes(ctx -> handleCommandSetHome(ctx, true))
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx -> handleCommandSetHome(ctx, false))
                        )
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("delhome")
                        .executes(ctx -> handleCommandDelHome(ctx, true))
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx -> handleCommandDelHome(ctx, false))
                        )
        ));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("home")
                        .executes(ctx -> handleCommandHome(ctx, true))
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx -> handleCommandHome(ctx, false))
                        )
        ));
    }

    @Override
    public void enable() {
        super.enable();

        initHomesFromDb();
    }

    @Override
    public void disable() {
        super.disable();

        Homes.clear();
    }

    private @NotNull IntegerConfigEntry getConfigMaxHomes() {
        return (IntegerConfigEntry) getConfigEntry("maxHomes");
    }

    private @NotNull BooleanConfigEntry getConfigAllowNether() {
        return (BooleanConfigEntry) getConfigEntry("allowNether");
    }

    private @NotNull BooleanConfigEntry getConfigAllowEnd() {
        return (BooleanConfigEntry) getConfigEntry("allowEnd");
    }

    private final String HOMES_TABLE_NAME = "QOL_Homes";
    public final Map<String, HomeEntry> Homes = new HashMap<>();

    private void initHomesFromDb() {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            Homes.clear();

            ResultSet rs = getEntries(st);
            while (rs.next()) {
                HomeEntry entry = new HomeEntry(rs);
                Homes.put(entry.Id(), entry);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private @NotNull Integer handleCommandSetHome(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull Boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            if (!checkSetHomeAllowedInDim(ctx)) return Command.SINGLE_SUCCESS;
            ServerPlayerEntity player = getPlayer(ctx);

            String tempName = "Home";
            if (!useDefaultName) tempName = StringArgumentType.getString(ctx, "name");
            String name = tempName;

            List<HomeEntry> homes = getHomesForPlayer(player);

            // Check if home already exists
            HomeEntry existingEntry = homes.stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry != null) {
                // Update Entry
                updateHome(player, existingEntry);
                ctx.getSource().sendFeedback(() -> Text.literal("§aHome \"" + name + "\" aktualisiert."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Check maxHomes exceeded
            if (homes.size() >= getConfigMaxHomes().Value) {
                // Reject -> maxHomes reached
                ctx.getSource().sendFeedback(() -> Text.literal("§cDu hast die maximale Anzahl an Homes erreicht."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Insert new Entry
            createHome(player, name);
            ctx.getSource().sendFeedback(() -> Text.literal("§aHome \"" + name + "\" erstellt."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Boolean checkSetHomeAllowedInDim(@NotNull CommandContext<ServerCommandSource> ctx) throws Exception {
        ServerPlayerEntity player = getPlayer(ctx);

        String dimension = player.getEntityWorld().getRegistryKey().getValue().toString();

        if ((dimension.equals("minecraft:the_nether") && !getConfigAllowNether().Value)
                || (dimension.equals("minecraft:the_end") && !getConfigAllowEnd().Value)) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDu darfst in dieser Dimension keine Homes erstellen."), false);
            return false;
        }

        return true;
    }

    private @NotNull Integer handleCommandDelHome(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull Boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            ServerPlayerEntity player = getPlayer(ctx);

            String tempName = "Home";
            if (!useDefaultName) tempName = StringArgumentType.getString(ctx, "name");
            String name = tempName;

            List<HomeEntry> homes = getHomesForPlayer(player);

            // Check if home exists
            HomeEntry existingEntry = homes.stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry == null) {
                // Reject -> home does not exist
                ctx.getSource().sendFeedback(() -> Text.literal("§cHome \"" + name + "\" existiert nicht."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Delete Entry
            removeHome(existingEntry);
            ctx.getSource().sendFeedback(() -> Text.literal("§aHome \"" + name + "\" §cgelöscht§a."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Integer handleCommandHome(@NotNull CommandContext<ServerCommandSource> ctx, @NotNull Boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS) return Command.SINGLE_SUCCESS;
            ServerPlayerEntity player = getPlayer(ctx);

            String tempName = "Home";
            if (!useDefaultName) tempName = StringArgumentType.getString(ctx, "name");
            String name = tempName;

            List<HomeEntry> homes = getHomesForPlayer(player);

            // Check if home exists
            HomeEntry existingEntry = homes.stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry == null) {
                // Reject -> home does not exist
                ctx.getSource().sendFeedback(() -> Text.literal("§cHome \"" + name + "\" existiert nicht."), false);
                return Command.SINGLE_SUCCESS;
            }

            // Teleport
            new Thread(() -> TeleportHelper.teleportPlayer(ctx, existingEntry.getDimension(), existingEntry.X, existingEntry.Y, existingEntry.Z)).start();
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull List<HomeEntry> getHomesForPlayer(@NotNull ServerPlayerEntity player) {
        List<HomeEntry> foundHomes = new ArrayList<>();

        Homes.forEach((id, entry) -> {
            if (entry.Owner.equals(player.getUuidAsString())) foundHomes.add(entry);
        });

        return foundHomes;
    }

    private void createHome(@NotNull ServerPlayerEntity player, @NotNull String name) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            HomeEntry entry = new HomeEntry(player.getUuidAsString(), name, player.getEntityWorld().getRegistryKey().getValue().toString(), player.getX(), player.getY(), player.getZ());
            ResultSet rs = insertEntry(st, entry);
            entry = new HomeEntry(rs);
            Homes.put(entry.Id(), entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void updateHome(@NotNull ServerPlayerEntity player, @NotNull HomeEntry entry) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            entry.X = player.getX();
            entry.Y = player.getY();
            entry.Z = player.getZ();
            ResultSet rs = updateEntry(st, entry);
            entry = new HomeEntry(rs);
            Homes.put(entry.Id(), entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void removeHome(@NotNull HomeEntry entry) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            deleteEntry(st, entry);
            Homes.remove(entry.Id());
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private @NotNull ResultSet getEntries(@NotNull Statement st) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + HOMES_TABLE_NAME + "';");
    }

    private @NotNull ResultSet getEntry(@NotNull Statement st, @NotNull HomeEntry entry) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + HOMES_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }

    private @NotNull ResultSet updateEntry(@NotNull Statement st, @NotNull HomeEntry entry) throws SQLException {
        st.execute("UPDATE '" + HOMES_TABLE_NAME + "' SET Dimension='" + entry.getDimension() + "',X=" + entry.X + ",Y=" + entry.Y + ",Z=" + entry.Z + " WHERE Id='" + entry.Id() + "';");
        return getEntry(st, entry);
    }

    private @NotNull ResultSet insertEntry(@NotNull Statement st, @NotNull HomeEntry entry) throws SQLException {
        st.execute("INSERT INTO '" + HOMES_TABLE_NAME + "' (Id,Owner,Name,Dimension,X,Y,Z) VALUES ('" + entry.Id() + "','" + entry.Owner + "','" + entry.Name + "','" + entry.getDimension() + "'," + entry.X + "," + entry.Y + "," + entry.Z + ");");
        return getEntry(st, entry);
    }

    private void deleteEntry(@NotNull Statement st, @NotNull HomeEntry entry) throws SQLException {
        st.execute("DELETE FROM '" + HOMES_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }
}
