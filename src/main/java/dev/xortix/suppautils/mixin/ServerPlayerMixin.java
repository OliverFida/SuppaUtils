package dev.xortix.suppautils.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.xortix.suppautils.main.shared.PlayerListManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    private Component modifyName(Component original) {
        return PlayerListManager.getPlayerListName((ServerPlayer) (Object) this);
    }
}

