package pepe.wins.DGGServerPlugin.commands.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayerManager;

import java.util.*;

public final class ProfileSubcommand implements Subcommand {

    private final DGGServerPlugin plugin;
    private final DggPlayerManager playerManager;

    public ProfileSubcommand(DGGServerPlugin plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    public String name() { return "profile"; }

    @Override
    public String permission() { return "dgg.command.profile"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player caller)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }
        if (args.length > 1) {
            caller.sendMessage(Component.text("Usage: /dgg profile", NamedTextColor.RED));
            return true;
        }

        UUID uuid = caller.getUniqueId();
        playerManager.get(uuid)
                .thenAccept(dp -> {
                    caller.sendMessage(Component.text(dp.toString(), NamedTextColor.GREEN));
                    caller.sendMessage(Component.text("DGG ID: " + dp.dggId(), NamedTextColor.GREEN));
                    caller.sendMessage(Component.text("Nickname: " + dp.nick(), NamedTextColor.GREEN));
                    caller.sendMessage(Component.text("Sub level: " + dp.subscription(), NamedTextColor.GREEN));
                    caller.sendMessage(Component.text("MC UUID: " + dp.uuid(), NamedTextColor.GREEN));
                    caller.sendMessage(Component.text("Roles: ", NamedTextColor.GREEN));
                    for(String s : dp.roles()){
                        caller.sendMessage(Component.text(s, NamedTextColor.GREEN));
                    }
                    caller.sendMessage(Component.text("Features: ", NamedTextColor.GREEN));
                    for(String s : dp.features()){
                        caller.sendMessage(Component.text(s, NamedTextColor.GREEN));
                    }



                })
                .exceptionally(ex -> {
                    caller.sendMessage(Component.text("Profile not found!", NamedTextColor.RED));
                    plugin.getLogger().warning("Error finding profile: " + ex.toString());
                    return null;
                });
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
}
