package dev.xortix.suppautils.main.shared;

import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.features.qol.afk.QolAfkFeatureProvider;
import dev.xortix.suppautils.main.features.qol.initials.InitialsEntry;
import dev.xortix.suppautils.main.features.qol.initials.QolInitialsFeatureProvider;
import java.util.Collection;
import java.util.EnumSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

public class PlayerListManager {
    public static void updatePlayerList() {
        if (Main.SERVER == null) return;

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
        Collection<ServerPlayer> players = Main.SERVER.getPlayerList().getPlayers();

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, players);
        Main.SERVER.getPlayerList().broadcastAll(packet);
    }

    public static void updatePlayerListEntryForPlayer(ServerPlayer player) {
        if (Main.SERVER == null) return;

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player);
        Main.SERVER.getPlayerList().broadcastAll(packet);
    }

    public static Component getPlayerListName(ServerPlayer player) {
        String username = player.getName().getString();
        MutableComponent customName = Component.empty();

        // AFK
        QolAfkFeatureProvider qolAfkFeatureProvider = (QolAfkFeatureProvider) FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_AFK);
        if (qolAfkFeatureProvider.PLAYERS_AFK.contains(player.getUUID())) customName.append("[AFK]").withStyle(ChatFormatting.GRAY);

        // INITIALS
        QolInitialsFeatureProvider qolInitialsFeatureProvider = (QolInitialsFeatureProvider) FeaturesManager.Features.get(FeaturesManager.FEATURE.QOL_INITIALS);
        InitialsEntry initials = qolInitialsFeatureProvider.getInitials().get(player.getUUID());
        if (initials != null) customName.append("[" + initials.Initials + "]");

        // Username
        if (!customName.toString().equals("empty")) customName.append(" ");
        customName.append(username);
        return customName;
    }
}
