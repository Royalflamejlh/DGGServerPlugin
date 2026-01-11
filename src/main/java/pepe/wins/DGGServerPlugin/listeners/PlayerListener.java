package pepe.wins.DGGServerPlugin.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getPlayerManager().get(p).thenAccept(dp -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Player still = Bukkit.getPlayer(p.getUniqueId());
                if (still != null) plugin.nametag().apply(still, dp);
                if(dp.dggId() <= 0) {
                    p.sendMessage(Component.text("Your DGG Account is not synced! Use:", NamedTextColor.RED));
                    p.sendMessage(Component.text("/dgg sync <dggusername>", NamedTextColor.RED));
                    p.sendMessage(Component.text("to sync your dgg account!", NamedTextColor.RED));
                }
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        plugin.nametag().clear(event.getPlayer());
        playerManager.save(p.getUniqueId())
                .exceptionally(ex -> { plugin.getLogger().warning("Failed to save player: " + ex); return null; });
    }
}
