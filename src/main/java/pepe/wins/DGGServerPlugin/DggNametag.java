package pepe.wins.DGGServerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import pepe.wins.DGGServerPlugin.lib.chat.ChatStyle;

import java.util.UUID;

public final class DggNametag {
    private final Scoreboard board;

    public DggNametag() {
        this.board = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /** Call on main thread. Colors the *real* username above head based on tier. */
    public void apply(Player p, DggPlayer dp) {
        Team t = teamFor(p.getUniqueId());
        t.color(ChatStyle.nametagColor(dp));

        t.prefix(net.kyori.adventure.text.Component.empty());
        t.suffix(net.kyori.adventure.text.Component.empty());

        // Ensure player is in the team (teams use entries = player name)
        if (!t.hasEntry(p.getName())) {
            // remove from any old dgg_ team we created
            cleanupOldTeams(p);
            t.addEntry(p.getName());
        }
    }

    /** Optional: cleanup on quit */
    public void clear(Player p) {
        cleanupOldTeams(p);
    }

    private Team teamFor(UUID uuid) {
        String name = teamName(uuid);
        Team t = board.getTeam(name);
        if (t == null) t = board.registerNewTeam(name);
        return t;
    }

    private void cleanupOldTeams(Player p) {
        for (Team team : board.getTeams()) {
            if (!team.getName().startsWith("dgg_")) continue;
            if (team.hasEntry(p.getName())) {
                team.removeEntry(p.getName());
                if (team.getEntries().isEmpty()) team.unregister();
            }
        }
    }

    private static String teamName(UUID uuid) {
        // Team name limit is 16 chars.
        String raw = uuid.toString().replace("-", "");
        return "dgg_" + raw.substring(0, 12); // 4 + 12 = 16
    }
}
