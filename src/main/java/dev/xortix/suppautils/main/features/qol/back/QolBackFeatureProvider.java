package dev.xortix.suppautils.main.features.qol.back;

import com.mojang.brigadier.Command;
import dev.xortix.suppautils.main.base.FeatureProviderBase;
import dev.xortix.suppautils.main.helpers.TeleportHelper;
import dev.xortix.suppautils.main.shared.commands.CommandsManager;
import dev.xortix.suppautils.main.shared.commands.FullyCustomCommand;
import dev.xortix.suppautils.main.shared.commands.SuppaCommand;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.commands.Commands.literal;

public class QolBackFeatureProvider extends FeatureProviderBase {
    @Override
    public @NotNull String getConfigCategory() {
        return "qol";
    }

    @Override
    public @NotNull String getConfigFeature() {
        return "back";
    }

    @Override
    protected void initImpl() {
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.ENABLE, this));
        CommandsManager.addToRegistrationList(new SuppaCommand(SuppaCommand.TYPE.DISABLE, this));
        CommandsManager.addToRegistrationList(new FullyCustomCommand(
                literal("back")
                        .executes(ctx -> {

                            try {
                                if (checkFeatureEnabledForCommand(ctx) == Command.SINGLE_SUCCESS)
                                    return Command.SINGLE_SUCCESS;

                                ServerPlayer player = ctx.getSource().getPlayer();
                                assert player != null;

                                TeleportHelper.teleportPlayerBack(ctx);
                                return Command.SINGLE_SUCCESS;
                            } catch (Exception ex) {
                                return handleCommandException(ex);
                            }
                        })
        ));
    }
}
