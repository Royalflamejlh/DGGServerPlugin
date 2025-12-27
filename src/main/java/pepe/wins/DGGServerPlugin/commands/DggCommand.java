package pepe.wins.DGGServerPlugin.commands;

import pepe.wins.DGGServerPlugin.commands.subcommands.Subcommand;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public final class DggCommand implements CommandExecutor, TabCompleter {
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public DggCommand(List<Subcommand> subs) {
        for (Subcommand s : subs) subcommands.put(s.name().toLowerCase(Locale.ROOT), s);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /dgg <subcommand> ...");
            sender.sendMessage(ChatColor.GRAY + "Try: /dgg tp pepe | /dgg tp pepe back | /dgg kill pepe");
            return true;
        }

        Subcommand sub = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
            return true;
        }

        String perm = sub.permission();
        if (perm != null && !perm.isBlank() && !sender.hasPermission(perm)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission: " + perm);
            return true;
        }

        // pass everything after the subcommand name to the subcommand
        String[] tail = Arrays.copyOfRange(args, 1, args.length);
        return sub.execute(sender, tail);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String k : subcommands.keySet()) {
                Subcommand s = subcommands.get(k);
                String perm = s.permission();
                if ((perm == null || perm.isBlank() || sender.hasPermission(perm)) && k.startsWith(prefix)) {
                    out.add(k);
                }
            }
            Collections.sort(out);
            return out;
        }

        Subcommand sub = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) return List.of();

        String[] tail = Arrays.copyOfRange(args, 1, args.length);
        return sub.tabComplete(sender, tail);
    }
}
