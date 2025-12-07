package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public final class FullyCustomCommand extends CommandBase {
    private final LiteralArgumentBuilder<ServerCommandSource> _builder;

    public FullyCustomCommand(@NotNull LiteralArgumentBuilder<ServerCommandSource> builder) {
        _builder = builder;
    }

    @Override
    public void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess, CommandManager.@NotNull RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(_builder);
    }
}
