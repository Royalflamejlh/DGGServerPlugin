package pepe.wins.DGGServerPlugin.commands.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatSubcommand implements Subcommand {

    private final DGGServerPlugin plugin;
    private final DggPlayerManager playerManager;

    public ChatSubcommand(DGGServerPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }


    @Override
    public String name() { return "chat"; }

    @Override
    public String permission() { return "dgg.command.chat"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player caller)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            caller.sendMessage(Component.text("Usage: /dgg chat <enable/disable>", NamedTextColor.RED));
            return true;
        }
        String req = args[0].trim().toLowerCase();
        if(req.equalsIgnoreCase("enable")){
            DggPlayer dp = playerManager.getCached(caller.getUniqueId());
            if(dp != null) dp.setDGGChat(true);
            else {
                caller.sendMessage(Component.text("Error enabling chat!" , NamedTextColor.RED));
            }
            return true;
        }
        else if(req.equalsIgnoreCase("disable")){
            DggPlayer dp = playerManager.getCached(caller.getUniqueId());
            if(dp != null) dp.setDGGChat(false);
            else {
                caller.sendMessage(Component.text("Error disabling chat!" , NamedTextColor.RED));
            }
            return true;
        }
        else{
            caller.sendMessage(Component.text("Usage: /dgg chat <enable/disable>", NamedTextColor.RED));
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> out = new ArrayList<>();
        out.add("enable");
        out.add("disable");
        return out;
    }
}
