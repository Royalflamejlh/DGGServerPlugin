package pepe.wins.DGGServerPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BedExplosionListener implements Listener {

    @EventHandler
    public void onBedUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();

        if (!type.name().endsWith("_BED")) return;

        World.Environment env = event.getPlayer().getWorld().getEnvironment();

        if (env == World.Environment.NETHER || env == World.Environment.THE_END) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Bedsplosions are not allowed NOPPERS!");
        }
    }
}
