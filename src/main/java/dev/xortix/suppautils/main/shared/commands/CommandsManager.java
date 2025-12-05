package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;

public class CommandsManager {
    private static final List<CommandBase> _commands = new ArrayList<>();

    public static void addToRegistrationList(CommandBase command) {
        _commands.add(command);
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        for (CommandBase command : _commands) {
            command.register(dispatcher, registryAccess, registrationEnvironment);
        }
    }
}
