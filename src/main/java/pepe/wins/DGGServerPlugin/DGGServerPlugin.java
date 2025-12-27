package pepe.wins.DGGServerPlugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pepe.wins.DGGServerPlugin.commands.DggCommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.KillSubcommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.Subcommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.TpSubcommand;
import pepe.wins.DGGServerPlugin.listeners.BedExplosionListener;

import java.util.List;

public final class DGGServerPlugin extends JavaPlugin {

    private DggPlayerManager dggPlayerManager;

    @Override
    public void onEnable() {
        getLogger().info("PEPE WINS - Loading DGGServerPlugin");
        saveDefaultConfig();
        dggPlayerManager = new DggPlayerManager();

        List<Subcommand> subs = List.of(
                new TpSubcommand(dggPlayerManager),
                new KillSubcommand(dggPlayerManager)
        );

        DggCommand dgg = new DggCommand(subs);

        PluginCommand cmd = getCommand("dgg");
        if (cmd == null) {
            getLogger().severe("Command 'dgg' not found in plugin.yml!");
            return;
        }
        cmd.setExecutor(dgg);
        cmd.setTabCompleter(dgg);

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

    public DggPlayerManager dggPlayerManager() {
        return dggPlayerManager;
    }

}
