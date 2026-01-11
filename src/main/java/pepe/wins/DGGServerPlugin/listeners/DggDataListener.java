package pepe.wins.DGGServerPlugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatListener;
import pepe.wins.DGGServerPlugin.lib.chat.DGGMessage;
import pepe.wins.DGGServerPlugin.lib.chat.ChatStyle;

import java.util.List;

public class DggDataListener implements DGGChatListener {
    private final DGGServerPlugin plugin;
    private final DggPlayerManager playerManager;

    public DggDataListener(DGGServerPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    public void onMsg(DGGMessage msg) {
        if (msg == null) return;

        DggPlayer sender_dp = playerManager.getCachedByDggId(msg.id); // cache-only
        if (sender_dp != null) {
            boolean changed = false;

            // --- nick
            String newNick = msg.nick;
            if (newNick == null) newNick = "";
            String oldNick = sender_dp.nick();
            if (oldNick == null) oldNick = "";
            if (!oldNick.equals(newNick)) {
                sender_dp.setNick(newNick);
                changed = true;
            }

            // --- subscription tier (msg.subscription can be null)
            int newTier = (msg.subscription == null) ? 0 : msg.subscription.tier;
            if (sender_dp.subscription() != newTier) {
                sender_dp.setTier(newTier);
                changed = true;
            }

            // --- roles
            if (!listEquals(sender_dp.roles(), msg.roles)) {
                sender_dp.setRoles(msg.roles == null ? List.of() : msg.roles);
                changed = true;
            }

            // --- features
            if (!listEquals(sender_dp.features(), msg.features)) {
                sender_dp.setFeatures(msg.features == null ? List.of() : msg.features);
                changed = true;
            }

            if (changed) {
                Player online = Bukkit.getPlayer(sender_dp.uuid());
                if (online != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.nametag().apply(online, sender_dp));
                }
                playerManager.save(sender_dp); // async save
            }
        }

        Component m = ChatStyle.line(msg, sender_dp);
        for (Player p : Bukkit.getOnlinePlayers()) {
            DggPlayer dp = playerManager.getCached(p);
            if (dp == null || !dp.isDggChatFlag()) continue;
            p.sendMessage(m);
        }
    }
    private static boolean listEquals(List<String> a, List<String> b) {
        if (a == b) return true;
        if (a == null) a = List.of();
        if (b == null) b = List.of();
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            String x = a.get(i);
            String y = b.get(i);
            if (x == null ? y != null : !x.equals(y)) return false;
        }
        return true;
    }
}

