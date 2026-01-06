package pepe.wins.DGGServerPlugin;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pepe.wins.DGGServerPlugin.commands.DggCommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.*;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChat;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatBus;
import pepe.wins.DGGServerPlugin.lib.db.Database;
import pepe.wins.DGGServerPlugin.listeners.BedExplosionListener;
import pepe.wins.DGGServerPlugin.listeners.DggDataListener;
import pepe.wins.DGGServerPlugin.listeners.PlayerListener;

import java.util.List;

public final class DGGServerPlugin extends JavaPlugin {

    private DGGChat dggChat;

    private DGGChatBus chatBus;

    private DggPlayerManager dggPlayerManager;

    private Database db;

    @Override
    public void onEnable() {
        getLogger().info("PEPE WINS - Loading DGGServerPlugin");

        db = new Database(this);
        chatBus = new DGGChatBus(this);
        dggPlayerManager = new DggPlayerManager(this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms api = provider.getProvider();
        } else {
            getLogger().severe("LuckPerms not found! Please make sure it exists!");
        }

        saveDefaultConfig();

        List<Subcommand> subs = List.of(
                new TpSubcommand(dggPlayerManager),
                new KillSubcommand(dggPlayerManager),
                new SyncSubcommand(chatBus, dggPlayerManager),
                new ProfileSubcommand(this),
                new ChatSubcommand(this)
        );

        DggCommand dgg = new DggCommand(subs);

        PluginCommand cmd = getCommand("dgg");
        if (cmd == null) {
            getLogger().severe("Command 'dgg' not found in plugin.yml!");
            return;
        }
        cmd.setExecutor(dgg);
        cmd.setTabCompleter(dgg);

        getServer().getPluginManager().registerEvents(new BedExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        dggChat = new DGGChat("wss://chat.destiny.gg/ws", "https://www.destiny.gg", null, chatBus);
        dggChat.connect();

        getLogger().info("DGGServerPlugin loaded");
    }

    @Override
    public void onDisable() {
        if (dggChat != null) {
            dggChat.disconnect();
        }

        if (dggPlayerManager != null) {
            dggPlayerManager.stopAutosave();     // optional but good
            dggPlayerManager.saveAllBlocking();  // guaranteed to run before disable completes
        }

        if (db != null) {
            db.close(); // make this call hikari.close()
        }

        getLogger().info("DGGServerPlugin unloaded");
    }


    public Database getDatabase() {
        return db;
    }

    public DggPlayerManager getPlayerManager() {
        return dggPlayerManager;
    }
}
