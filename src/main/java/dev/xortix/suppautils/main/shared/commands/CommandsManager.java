package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.xortix.suppautils.main.base.CommandBase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CommandsManager {
    private static final List<CommandBase> _commands = new ArrayList<>();

    public static void addToRegistrationList(@NotNull CommandBase command) {
        _commands.add(command);
    }

    public static void registerCommands(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess, @NotNull CommandManager.RegistrationEnvironment registrationEnvironment) {
        for (CommandBase command : _commands) {
            command.register(dispatcher, registryAccess, registrationEnvironment);
        }
    }
}
