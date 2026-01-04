package pepe.wins.DGGServerPlugin;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DggPlayerManager {
    private final Map<UUID, DggPlayer> players = new ConcurrentHashMap<>();

    public DggPlayer getOrCreate(UUID uuid) {
        return players.computeIfAbsent(uuid, DggPlayer::new);
    }

    public DggPlayer getOrCreate(Player p) {
        return getOrCreate(p.getUniqueId());
    }

    public void refresh(Player p) {
        DggPlayer dp = getOrCreate(p);
        dp.updatePermissionTeam();
    }

    public DggPlayer get(long dggId) {
        for(DggPlayer dp : players.values()){
            if(dp.getDggId() == dggId) return dp;
        }
        return null;
    }
}
