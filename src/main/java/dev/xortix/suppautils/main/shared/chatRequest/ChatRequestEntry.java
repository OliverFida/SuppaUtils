package dev.xortix.suppautils.main.shared.chatRequest;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public final class ChatRequestEntry {
    public final long Timestamp;
    public final String Sender;
    public final String Receiver;
    public final BiFunction<@NotNull CommandContext<ServerCommandSource>, @NotNull ChatRequestEntry, @NotNull Integer> ExecuteAccept;
    public final BiFunction<@NotNull CommandContext<ServerCommandSource>, @NotNull ChatRequestEntry, @NotNull Integer> ExecuteDeny;

    public ChatRequestEntry(@NotNull ServerPlayerEntity sender, @NotNull ServerPlayerEntity receiver, @NotNull BiFunction<@NotNull CommandContext<ServerCommandSource>, @NotNull ChatRequestEntry, @NotNull Integer> executeAccept, @NotNull BiFunction<@NotNull CommandContext<ServerCommandSource>, @NotNull ChatRequestEntry, @NotNull Integer> executeDeny) {
        Timestamp = System.currentTimeMillis();
        Sender = sender.getUuidAsString();
        Receiver = receiver.getUuidAsString();
        ExecuteAccept = executeAccept;
        ExecuteDeny = executeDeny;
    }
}
