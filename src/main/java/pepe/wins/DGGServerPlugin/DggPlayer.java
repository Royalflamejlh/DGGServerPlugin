package pepe.wins.DGGServerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public final class DggPlayer {
    private final UUID uuid;
    private DggTeam team;
    private String nick;
    private long dggId;
    private List<String> roles;
    private List<String> features;
    private String subscription;
    private BlockingDeque<Location> locationStack = new LinkedBlockingDeque<>();

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

    public Location popLocation() {
        return locationStack.pop();
    }

    public void pushLocation(@NotNull Location location) {
        locationStack.push(location);
    }

    public void setDggId(long id){
        this.dggId = id;
    }

    public long getDggId() {
        return dggId;
    }
}
