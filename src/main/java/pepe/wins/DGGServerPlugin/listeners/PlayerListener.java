package pepe.wins.DGGServerPlugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;

public final class PlayerListener implements Listener {

    private final DGGServerPlugin plugin;
    private final DggPlayerManager playerManager;

    public PlayerListener(DGGServerPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        playerManager.get(p.getUniqueId())
                .exceptionally(ex -> { plugin.getLogger().warning("Failed to load player: " + ex); return null; });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        playerManager.save(p.getUniqueId())
                .exceptionally(ex -> { plugin.getLogger().warning("Failed to save player: " + ex); return null; });
    }
}
