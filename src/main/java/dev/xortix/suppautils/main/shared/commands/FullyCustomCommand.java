package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class FullyCustomCommand extends CommandBase {
    private final LiteralArgumentBuilder<ServerCommandSource> _builder;

    public FullyCustomCommand(LiteralArgumentBuilder<ServerCommandSource> builder) {
        _builder = builder;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(_builder);
    }
}
