package dev.xortix.suppautils.main.shared.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

public final class FullyCustomCommand extends CommandBase {
    private final LiteralArgumentBuilder<CommandSourceStack> _builder;

    public FullyCustomCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> builder) {
        _builder = builder;
    }

    @Override
    public void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, @NotNull CommandBuildContext registryAccess, @NotNull Commands.CommandSelection registrationEnvironment) {
        dispatcher.register(_builder);
    }
}
