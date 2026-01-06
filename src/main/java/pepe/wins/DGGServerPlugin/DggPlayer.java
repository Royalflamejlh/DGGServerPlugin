package pepe.wins.DGGServerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pepe.wins.DGGServerPlugin.commands.subcommands.Subcommand;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public final class DggPlayer {

    // ----------- stored profile stuff
    private final UUID uuid;
    private String nick;
    private long dggId;
    private List<String> roles;
    private List<String> features;
    private int subscription;

    // ------------------- unstored profile stuff
    private boolean dggChatFlag = false;
    private boolean dggDebugFlag = false;
    private BlockingDeque<Location> locationStack = new LinkedBlockingDeque<>();

    public DggPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
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

    public long dggId() {
        return dggId;
    }

    public String nick() {
        return nick;
    }

    public List<String> roles() {
        return roles;
    }

    public List<String> features() {
        return features;
    }

    public int subscription() {
        return subscription;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public void setSubscription(int subscription) {
        this.subscription = subscription;
    }

    public boolean isDggChatFlag() {
        return dggChatFlag;
    }

    public boolean isDggDebugFlag() {
        return dggChatFlag;
    }

    public void setDGGChat(boolean b) {
        dggChatFlag = b;
    }

    public void setDGGDebug(boolean b) {
        dggDebugFlag = b;
    }
}
