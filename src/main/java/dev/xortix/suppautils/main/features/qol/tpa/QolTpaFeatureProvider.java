package dev.xortix.suppautils.main.features.qol.tpa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.helpers.TeleportHelper;
import dev.xortix.suppautils.main.shared.chatRequest.ChatRequestEntry;
import dev.xortix.suppautils.main.shared.chatRequest.ChatRequestManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class QolTpaFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "tpa";
    }

    @Override
    protected void initImpl() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("tpa")
                        .then(argument("player", GameProfileArgumentType.gameProfile())
                                .executes(this::handleCommandTpa)
                        )
        ));
    }

    private @NotNull Integer handleCommandTpa(CommandContext<ServerCommandSource> ctx) {
        try {
            if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                return Command.SINGLE_SUCCESS;
            ServerPlayerEntity sender = getPlayer(ctx);

            Collection<PlayerConfigEntry> receivers = GameProfileArgumentType.getProfileArgument(ctx, "player");
            if (receivers.size() != 1) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cEs muss exakt ein Ziel angeführt werden."), false);
                return 0;
            }

            PlayerConfigEntry receivingConfig = receivers.stream().findFirst().orElse(null);
            if (receivingConfig.id().equals(sender.getUuid())) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cDu kannst dich nicht zu dir selbst teleportieren."), false);
                return 0;
            }
            ServerPlayerEntity receiver = Main.SERVER.getPlayerManager().getPlayer(receivingConfig.id());
            if (receiver == null) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cZiel konnte nicht gefunden werden."), false);
                return 0;
            }

            ChatRequestEntry request = new ChatRequestEntry(sender, receiver, this::handleAccept, this::handleDeny);
            ChatRequestManager.addRequest(request);

            receiver.sendMessage(Text.literal("§6" + sender.getStringifiedName() + " möchte sich zu dir teleportieren.\nDie Anfrage läuft in 30 Sekunden ab.\nNutze /accept zum annehmen, oder /deny zum ablehnen."));

            ctx.getSource().sendFeedback(() -> Text.literal("§6Anfrage an " + receivingConfig.name() + " gesendet."), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Integer handleAccept(CommandContext<ServerCommandSource> ctx, ChatRequestEntry request) {
        try {
            ServerPlayerEntity sender = Main.SERVER.getPlayerManager().getPlayer(UUID.fromString(request.Sender));
            if (sender == null) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cEtwas ist schief gelaufen..."), false);
                return 0;
            }
            ServerPlayerEntity receiver = getPlayer(ctx);

            ctx.getSource().sendFeedback(() -> Text.literal("§6Du hast die Anfrage §aangenommen§6."), false);
            sender.sendMessage(Text.literal("§6" + receiver.getStringifiedName() + " hat deine Anfrage §aangenommen§6."));
            TeleportHelper.teleportPlayerToPlayer(sender, receiver);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private @NotNull Integer handleDeny(CommandContext<ServerCommandSource> ctx, ChatRequestEntry request) {
        try {
            ServerPlayerEntity receiver = getPlayer(ctx);
            ServerPlayerEntity sender = Main.SERVER.getPlayerManager().getPlayer(UUID.fromString(request.Sender));
            if (sender == null) {
                ctx.getSource().sendFeedback(() -> Text.literal("§cEtwas ist schief gelaufen..."), false);
                return 0;
            }

            ctx.getSource().sendFeedback(() -> Text.literal("§6Du hast die Anfrage §cabgelehnt§6."), false);
            sender.sendMessage(Text.literal("§6" + receiver.getStringifiedName() + " hat deine Anfrage §cabgelehnt§6."));
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }
}
