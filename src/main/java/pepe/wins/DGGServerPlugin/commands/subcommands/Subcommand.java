package pepe.wins.DGGServerPlugin.commands.subcommands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand {
    String name();                      // "tp", "kill", ...
    String permission();                // permission to use subcommand (optional)
    boolean execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
}
