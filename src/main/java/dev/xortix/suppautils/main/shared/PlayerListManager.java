package dev.xortix.suppautils.main.shared;

import dev.xortix.suppautils.main.Main;
import dev.xortix.suppautils.main.afk.AfkProvider;
import dev.xortix.suppautils.main.initials.InitialsProvider;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.EnumSet;

public class PlayerListManager {
    public static void updatePlayerList() {
        if (Main.SERVER == null) return;

        EnumSet<PlayerListS2CPacket.Action> actions = EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME);
        Collection<ServerPlayerEntity> players = Main.SERVER.getPlayerManager().getPlayerList();

        PlayerListS2CPacket packet = new PlayerListS2CPacket(actions, players);
        Main.SERVER.getPlayerManager().sendToAll(packet);
    }

    public static void updatePlayerListEntryForPlayer(ServerPlayerEntity player) {
        if (Main.SERVER == null) return;

        PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
        Main.SERVER.getPlayerManager().sendToAll(packet);
    }

    public static Text getPlayerListName(ServerPlayerEntity player) {
        String username = player.getName().getString();
        MutableText customName = Text.empty();

        // AFK
        if (AfkProvider.PLAYERS_AFK.contains(player.getUuid())) customName.append("[AFK]").formatted(Formatting.GRAY);

        // INITIALS
        String initials = InitialsProvider.INITIALS.get(username);
        if (initials != null) customName.append("[" + initials + "]");

        // Username
        if (!customName.toString().equals("empty")) customName.append(" ");
        customName.append(username);
        return customName;
    }
}
