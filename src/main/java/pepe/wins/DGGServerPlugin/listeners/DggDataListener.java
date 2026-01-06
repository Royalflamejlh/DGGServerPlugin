package pepe.wins.DGGServerPlugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatBus;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatListener;
import pepe.wins.DGGServerPlugin.lib.chat.DGGMessage;

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
        DggPlayer sender_dp = playerManager.getCachedByDggId(msg.id); //todo: update db too
        if(sender_dp != null) {
            sender_dp.setNick(msg.nick);
            sender_dp.setFeatures(msg.features);
            sender_dp.setSubscription(msg.subscription.tier);
            sender_dp.setRoles(msg.roles);
            playerManager.save(sender_dp);
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            DggPlayer dp = playerManager.getCached(p);
            if(dp == null || !dp.isDggChatFlag()) continue;
            Component m = Component.text(msg.nick + ": " + msg.data, NamedTextColor.GRAY);
            p.sendMessage(m);
        }
    }
}

