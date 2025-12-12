package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.xortix.suppautils.main.log.Logger;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBuilderBase {
    protected static @NotNull Integer handleCommandException(@NotNull Exception ex) {
        Logger.log(Logger.LogCategory.GLOBAL, Logger.LogType.ERROR, "Error during command execution: " + ex.getMessage());
        return 0;
    }

    protected static @NotNull ServerPlayerEntity getPlayer(CommandContext<ServerCommandSource> ctx) throws Exception {
        ServerPlayerEntity temp = ctx.getSource().getPlayer();
        if (temp == null)
            throw new Exception("Player in context not found");
        return temp;
    }
}
