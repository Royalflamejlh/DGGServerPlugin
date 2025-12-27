package pepe.wins.DGGServerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class DggPlayer {
    private final UUID uuid;
    private DggTeam team;

    private String dggchatname;

    public DggPlayer(UUID uuid) {
        this.uuid = uuid;
        updatePermissionTeam();
    }

    public UUID uuid() {
        return uuid;
    }

    public void updatePermissionTeam() {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            if (p.hasPermission("dgg.team.jannie")) team = DggTeam.JANNIE;
            else if (p.hasPermission("dgg.team.pepe")) team = DggTeam.PEPE;
            else if (p.hasPermission("dgg.team.yee")) team = DggTeam.YEE;
            else team = DggTeam.HOMELESS;
        }
    }

    public DggTeam getTeam(){
        return team;
    }

}
