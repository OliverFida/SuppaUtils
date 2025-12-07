package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBase extends CommandBuilderBase {
    public abstract void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, @NotNull CommandRegistryAccess registryAccess, @NotNull CommandManager.RegistrationEnvironment registrationEnvironment);
}
