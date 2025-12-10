package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public abstract class CommandBase extends CommandBuilderBase {
    public abstract void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext registryAccess, @NotNull Commands.CommandSelection registrationEnvironment);
}
