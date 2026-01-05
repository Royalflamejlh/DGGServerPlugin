package pepe.wins.DGGServerPlugin;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pepe.wins.DGGServerPlugin.commands.DggCommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.KillSubcommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.Subcommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.SyncSubcommand;
import pepe.wins.DGGServerPlugin.commands.subcommands.TpSubcommand;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChat;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatBus;
import pepe.wins.DGGServerPlugin.listeners.BedExplosionListener;

import java.util.List;

public final class DGGServerPlugin extends JavaPlugin {

    private DGGChat dggChat;

    private final DGGChatBus chatBus = new DGGChatBus(this);

    private final DggPlayerManager dggPlayerManager  = new DggPlayerManager();

    @Override
    public void onEnable() {
        getLogger().info("PEPE WINS - Loading DGGServerPlugin");

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
                new SyncSubcommand(chatBus, dggPlayerManager)
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

        dggChat = new DGGChat("wss://chat.destiny.gg/ws", "https://www.destiny.gg", null, chatBus);
        dggChat.connect();
        getLogger().info("DGGServerPlugin loaded");
    }

    @Override
    public void onDisable() {
        dggChat.disconnect();
        getLogger().info("DGGServerPlugin unloaded");
    }

    public DggPlayerManager dggPlayerManager() {
        return dggPlayerManager;
    }

}
