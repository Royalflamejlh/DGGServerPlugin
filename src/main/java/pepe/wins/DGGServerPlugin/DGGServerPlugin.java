package pepe.wins.DGGServerPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import pepe.wins.DGGServerPlugin.listeners.BedExplosionListener;

public final class DGGServerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("PEPE WINS LOADING DGGServerPlugin");
        getServer().getPluginManager().registerEvents(
                new BedExplosionListener(), this
        );
        getLogger().info("DGGServerPlugin loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
