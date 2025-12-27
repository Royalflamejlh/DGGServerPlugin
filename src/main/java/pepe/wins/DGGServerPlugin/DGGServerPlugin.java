package pepe.wins.DGGServerPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import pepe.wins.DGGServerPlugin.listeners.BedExplosionListener;

public final class DGGServerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("PEPE WINS - Loading DGGServerPlugin");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(
                new BedExplosionListener(this), this
        );
        getLogger().info("DGGServerPlugin loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("DGGServerPlugin unloaded");
    }
}
