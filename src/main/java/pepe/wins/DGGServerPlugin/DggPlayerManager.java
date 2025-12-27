package pepe.wins.DGGServerPlugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DggPlayerManager {
    private final Map<UUID, DggPlayer> players = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastTpFrom = new ConcurrentHashMap<>();

    public DggPlayer getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, DggPlayer::new);
    }

    public DggPlayer getOrCreate(Player p) {
        return getOrCreate(p.getUniqueId());
    }

    /** Call when you want the team re-evaluated from permissions. */
    public void refresh(Player p) {
        DggPlayer dp = getOrCreate(p);
        dp.updatePermissionTeam();
    }

    public void rememberLastLocation(Player p) {
        lastTpFrom.put(p.getUniqueId(), p.getLocation().clone());
    }

    public Location popLastLocation(Player p) {
        return lastTpFrom.remove(p.getUniqueId());
    }

    public Location peekLastLocation(Player p) {
        return lastTpFrom.get(p.getUniqueId());
    }
}
