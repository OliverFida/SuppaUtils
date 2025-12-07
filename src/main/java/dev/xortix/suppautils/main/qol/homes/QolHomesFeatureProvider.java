package dev.xortix.suppautils.main.qol.homes;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.config.BooleanConfigEntry;
import dev.xortix.suppautils.main.config.IntegerConfigEntry;
import dev.xortix.suppautils.main.db.DBProvider;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class QolHomesFeatureProvider extends FeatureProviderBase {
    @Override
    public String getConfigCategory() {
        return "qol";
    }

    @Override
    public String getConfigFeature() {
        return "homes";
    }

    @Override
    public void init() {
        initHomesFromDb();

        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "countdown", IntegerArgumentType.integer(0, 30), "seconds"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "cooldown", IntegerArgumentType.integer(0, 3600), "seconds"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "maxHomes", IntegerArgumentType.integer(1, 100), "amount"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "allowNether", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "allowEnd", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "interDim", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.CONFIG, this, "back", BoolArgumentType.bool(), "enabled"));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("homes")
                        .executes(ctx -> {
                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                                    return Command.SINGLE_SUCCESS;

                                ServerPlayerEntity player = ctx.getSource().getPlayer();

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
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("back")
                        .executes(ctx -> {

                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                                    return Command.SINGLE_SUCCESS;
                                if (!getConfigBack().Value) {
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cDieses Feature wurde vom Admin deaktiviert."), false);
                                    return Command.SINGLE_SUCCESS;
                                }

                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                assert player != null;
                                Vec3d lastPosition = LAST_POSITION.get(player.getUuid());
                                if (lastPosition == null) {
                                    ctx.getSource().sendFeedback(() -> Text.literal("§cKeine letzte Position bekannt."), false);
                                    return Command.SINGLE_SUCCESS;
                                }

                                new Thread(() -> teleportPlayer(ctx, player.getEntityWorld().getRegistryKey().getValue().toString(), lastPosition.x, lastPosition.y, lastPosition.z)).start();

                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
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
        LAST_TELEPORT.clear();
    }

    private IntegerConfigEntry getConfigCountdown() {
        return (IntegerConfigEntry) getConfigEntry("countdown");
    }

    private IntegerConfigEntry getConfigCooldown() {
        return (IntegerConfigEntry) getConfigEntry("cooldown");
    }

    private IntegerConfigEntry getConfigMaxHomes() {
        return (IntegerConfigEntry) getConfigEntry("maxHomes");
    }

    private BooleanConfigEntry getConfigAllowNether() {
        return (BooleanConfigEntry) getConfigEntry("allowNether");
    }

    private BooleanConfigEntry getConfigAllowEnd() {
        return (BooleanConfigEntry) getConfigEntry("allowEnd");
    }

    private BooleanConfigEntry getConfigInterDim() {
        return (BooleanConfigEntry) getConfigEntry("interDim");
    }

    private BooleanConfigEntry getConfigBack() {
        return (BooleanConfigEntry) getConfigEntry("back");
    }

    private final String HOMES_TABLE_NAME = "QOL_Homes";
    public Map<String, HomeEntry> Homes = new HashMap<>();
    public final Map<UUID, Long> LAST_TELEPORT = new HashMap<>();
    public final Map<UUID, Vec3d> LAST_POSITION = new HashMap<>();

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

    private int handleCommandSetHome(CommandContext<ServerCommandSource> ctx, boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                return Command.SINGLE_SUCCESS;
            if (!checkSetHomeAllowedInDim(ctx))
                return Command.SINGLE_SUCCESS;

            ServerPlayerEntity player = ctx.getSource().getPlayer();
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

    private boolean checkSetHomeAllowedInDim(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        assert player != null;

        String dimension = player.getEntityWorld().getRegistryKey().getValue().toString();

        if ((dimension.equals("minecraft:the_nether") && !getConfigAllowNether().Value)
                || (dimension.equals("minecraft:the_end") && !getConfigAllowEnd().Value)) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDu darfst in dieser Dimension keine Homes erstellen."), false);
            return false;
        }

        return true;
    }

    private int handleCommandDelHome(CommandContext<ServerCommandSource> ctx, boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                return Command.SINGLE_SUCCESS;

            ServerPlayerEntity player = ctx.getSource().getPlayer();
            String tempName = "Home";
            if (!useDefaultName) tempName = StringArgumentType.getString(ctx, "name");
            String name = tempName;

            List<HomeEntry> homes = getHomesForPlayer(player);

            // Check if home exists
            HomeEntry existingEntry = homes.stream().filter(h -> h.Name.equalsIgnoreCase(name)).findFirst().orElse(null);
            if (existingEntry == null) {
                // Reject -> home does not exist
                ctx.getSource().sendFeedback(() -> Text.literal("§cHome\"" + name + "\" existiert nicht."), false);
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

    private int handleCommandHome(CommandContext<ServerCommandSource> ctx, boolean useDefaultName) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                return Command.SINGLE_SUCCESS;

            ServerPlayerEntity player = ctx.getSource().getPlayer();
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
            new Thread(() -> teleportPlayer(ctx, existingEntry.getDimension(), existingEntry.X, existingEntry.Y, existingEntry.Z)).start();
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private List<HomeEntry> getHomesForPlayer(ServerPlayerEntity player) {
        List<HomeEntry> foundHomes = new ArrayList<>();

        Homes.forEach((id, entry) -> {
            if (entry.Owner.equals(player.getUuidAsString())) foundHomes.add(entry);
        });

        return foundHomes;
    }

    private void teleportPlayer(CommandContext<ServerCommandSource> ctx, String dimension, double x, double y, double z) {
        try {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            assert player != null;
            int countdownSeconds = getConfigCountdown().Value;
            // OFDO: Safety checks: WillSuffocate, BlockUnderneath

            // Check cooldown
            long lastTeleport = LAST_TELEPORT.getOrDefault(player.getUuid(), 0L);
            if (System.currentTimeMillis() - lastTeleport < getConfigCooldown().Value * 1000) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cDu hast dich erst vor kurzem teleportiert."), false);
                return;
            }

            // Check interDim
            if (!player.getEntityWorld().getRegistryKey().getValue().toString().equals(dimension) && !getConfigInterDim().Value) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cDu darfst dich nicht zwischen Dimensionen teleportieren!"), false);
                return;
            }

            Vec3d positionBefore = player.getEntityPos();
            ctx.getSource().sendFeedback(() -> Text.literal("§6Teleportationsvorgang startet... Nicht bewegen!"), false);
            for (int s = 0; s < countdownSeconds; s++) {
                int restSeconds = countdownSeconds - s;
                ctx.getSource().sendFeedback(() -> Text.literal("§6" + restSeconds + "..."), false);
                Thread.sleep(1000);

                Vec3d positionAfter = player.getEntityPos();
                if (!positionBefore.equals(positionAfter)) {
                    ctx.getSource().sendFeedback(() -> Text.literal("§cAbbruch... Du hast dich bewegt."), false);
                    return;
                }
            }

            ServerWorld world = Main.SERVER.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimension)));
            assert world != null;

            Set<PositionFlag> flags = new HashSet<>();
            ctx.getSource().sendFeedback(() -> Text.literal("§6Teleportiere..."), false);
            boolean success = player.teleport(world, x, y, z, flags, player.getYaw(), player.getPitch(), false);
            if (!success) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cTeleportieren fehlgeschlagen."), false);
            } else {
                LAST_TELEPORT.put(player.getUuid(), System.currentTimeMillis());
                LAST_POSITION.put(player.getUuid(), positionBefore);
            }
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void createHome(ServerPlayerEntity player, String name) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            HomeEntry entry = new HomeEntry(player.getUuidAsString(), name, player.getEntityWorld().getRegistryKey().getValue().toString(), player.getX(), player.getY(), player.getZ());
            ResultSet rs = insertEntry(st, entry);
            entry = new HomeEntry(rs);
            Homes.put(entry.Id(), entry);
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private void updateHome(ServerPlayerEntity player, HomeEntry entry) {
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

    private void removeHome(HomeEntry entry) {
        try (Statement st = DBProvider.getCONNECTION().createStatement()) {
            deleteEntry(st, entry);
            Homes.remove(entry.Id());
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.HOMES, Logger.LogType.ERROR, ex.getMessage());
        }
    }

    private ResultSet getEntries(Statement st) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + HOMES_TABLE_NAME + "';");
    }

    private ResultSet getEntry(Statement st, HomeEntry entry) throws SQLException {
        return st.executeQuery("SELECT * FROM '" + HOMES_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }

    private ResultSet updateEntry(Statement st, HomeEntry entry) throws SQLException {
        st.execute("UPDATE '" + HOMES_TABLE_NAME + "' SET Dimension='" + entry.getDimension() + "',X=" + entry.X + ",Y=" + entry.Y + ",Z=" + entry.Z + " WHERE Id='" + entry.Id() + "';");
        return getEntry(st, entry);
    }

    private ResultSet insertEntry(Statement st, HomeEntry entry) throws SQLException {
        st.execute("INSERT INTO '" + HOMES_TABLE_NAME + "' (Id,Owner,Name,Dimension,X,Y,Z) VALUES ('" + entry.Id() + "','" + entry.Owner + "','" + entry.Name + "','" + entry.getDimension() + "'," + entry.X + "," + entry.Y + "," + entry.Z + ");");
        return getEntry(st, entry);
    }

    private void deleteEntry(Statement st, HomeEntry entry) throws SQLException {
        st.execute("DELETE FROM '" + HOMES_TABLE_NAME + "' WHERE Id='" + entry.Id() + "';");
    }
}
