package pepe.wins.DGGServerPlugin.commands.subcommands;

import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.DggTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KillSubcommand implements Subcommand {
    private final DggPlayerManager dggPlayerManager;

    public KillSubcommand(DggPlayerManager dggPlayerManager) {
        this.dggPlayerManager = dggPlayerManager;
    }

    @Override
    public String name() { return "kill"; }

    @Override
    public String permission() { return "dgg.command.kill"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player caller)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }
        if (args.length < 1) {
            caller.sendMessage(ChatColor.YELLOW + "Usage: /dgg kill <team>");
            return true;
        }

        DggTeam team = DggTeam.fromArg(args[0]).orElse(null);
        if (team == null || team == DggTeam.HOMELESS) {
            caller.sendMessage(ChatColor.RED + "Unknown team: " + args[0]);
            return true;
        }

        int affected = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            DggPlayer dp = dggPlayerManager.getCached(p);
//            if (dp.getTeam() == team) {
//                p.setHealth(0.0);
//                affected++;
//            }
        }

        caller.sendMessage(ChatColor.GREEN + "Killed " + affected + " " + team.id() + " player(s).");
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
        return List.of();
    }
}
