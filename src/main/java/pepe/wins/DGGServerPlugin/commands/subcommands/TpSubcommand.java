package pepe.wins.DGGServerPlugin.commands.subcommands;

import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.DggTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TpSubcommand implements Subcommand {
    private final DggPlayerManager dggPlayerManager;

    public TpSubcommand(DggPlayerManager dggPlayerManager) {
        this.dggPlayerManager = dggPlayerManager;
    }

    @Override
    public String name() { return "tp"; }

    @Override
    public String permission() { return "dgg.command.tp"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player caller)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /dgg tp <team> [back]");
            return true;
        }

        DggTeam team = DggTeam.fromArg(args[0]).orElse(null);
        if (team == null || team == DggTeam.HOMELESS) {
            sender.sendMessage(ChatColor.RED + "Unknown team: " + args[0]);
            return true;
        }

        boolean back = args.length >= 2 && args[1].equalsIgnoreCase("back");

        int affected = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(caller.getUniqueId())) continue;

            // refresh from permissions so it stays correct
            dggPlayerManager.refresh(p);
            DggPlayer dp = dggPlayerManager.getOrCreate(p);

            if (dp.getTeam() != team) continue;

            if (!back) {
                // remember where they were, then teleport to caller
                dggPlayerManager.rememberLastLocation(p);
                p.teleport(caller.getLocation());
                affected++;
            } else {
                Location from = dggPlayerManager.popLastLocation(p);
                if (from != null) {
                    p.teleport(from);
                    affected++;
                }
            }
        }

        if (!back) {
            caller.sendMessage(ChatColor.GREEN + "Teleported " + affected + " " + team.id() + " player(s) to you.");
        } else {
            caller.sendMessage(ChatColor.GREEN + "Sent back " + affected + " " + team.id() + " player(s).");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (DggTeam t : DggTeam.values()) {
                if (t == DggTeam.HOMELESS) continue;
                if (t.id().startsWith(p)) out.add(t.id());
            }
            return out;
        }
        if (args.length == 2) {
            if ("back".startsWith(args[1].toLowerCase(Locale.ROOT))) return List.of("back");
        }
        return List.of();
    }
}
