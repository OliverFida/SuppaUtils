package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CommandsManager {
    private static final List<CommandBase> _commands = new ArrayList<>();

    public static void addToRegistrationList(@NotNull CommandBase command) {
        _commands.add(command);
    }

    public static void registerCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext registryAccess, @NotNull Commands.CommandSelection registrationEnvironment) {
        for (CommandBase command : _commands) {
            command.register(dispatcher, registryAccess, registrationEnvironment);
        }
    }
}
