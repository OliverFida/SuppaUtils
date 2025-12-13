package dev.xortix.suppautils.main.shared.chatRequest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.base.CommandBuilderBase;
import dev.xortix.suppautils.main.log.Logger;
import dev.xortix.suppautils.main.shared.FeaturesManager;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public final class ChatRequestManager extends CommandBuilderBase {
    private static boolean isInitializing, isInitialized = false;
    private static final Map<String, ChatRequestEntry> _requests = new HashMap<>();

    public static void init() {
        try {
            if (isInitializing || isInitialized) return;
            isInitializing = true;

            CommandsManager.addToRegistrationList(new FullyCustomCommand(
                    literal("accept")
                            .executes(ChatRequestManager::handleCommandAccept)
            ));
            CommandsManager.addToRegistrationList(new FullyCustomCommand(
                    literal("deny")
                            .executes(ChatRequestManager::handleCommandDeny)
            ));

            isInitialized = true;
        } catch (Exception ex) {
            Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Initialization of TeleportHelper failed: " + ex.getMessage());
        } finally {
            isInitializing = false;
        }
    }

    public static void addRequest(ChatRequestEntry request) {
        _requests.put(request.Receiver, request);
    }

    public static void clearChaches() {
        if (FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_TPA).getIsEnabled()) return;

        _requests.clear();
    }

    private static ChatRequestEntry getRequest(ServerPlayerEntity receiver) {
        return _requests.get(receiver.getUuidAsString());
    }

    private static void removeRequest(ServerPlayerEntity receiver) {
        _requests.remove(receiver.getUuidAsString());
    }

    private static @NotNull Integer handleCommandAccept(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity receiver = getPlayer(ctx);

            ChatRequestEntry request = getRequest(receiver);
            if (!checkRequestForCommand(ctx, request)) return 0;

            int result = request.ExecuteAccept.apply(ctx, request);
            if (result != Command.SINGLE_SUCCESS) return result;

            removeRequest(receiver);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private static @NotNull Integer handleCommandDeny(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerPlayerEntity receiver = getPlayer(ctx);

            ChatRequestEntry request = getRequest(receiver);
            if (!checkRequestForCommand(ctx, request)) return 0;

            int result = request.ExecuteDeny.apply(ctx, request);
            if (result != Command.SINGLE_SUCCESS) return result;

            removeRequest(receiver);
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            return handleCommandException(ex);
        }
    }

    private static @NotNull Boolean checkRequestForCommand(CommandContext<ServerCommandSource> ctx, ChatRequestEntry request) {
        if (request == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDu hast keine offene Anfrage."), false);
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - request.Timestamp > 30 * 1000) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cDeine letzte Anfrage ist abgelaufen."), false);
            return false;
        }

        return true;
    }
}
