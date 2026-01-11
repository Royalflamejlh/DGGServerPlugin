package pepe.wins.DGGServerPlugin;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import pepe.wins.DGGServerPlugin.lib.chat.DGGMessage;

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
    private int tier;
    private boolean dggChatFlag = false;

    // ------------------- unstored profile stuff
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
        return tier;
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

    public void setSubscription(DGGMessage.Subscription subscription) {
        int tier = 0;
        if(subscription != null) tier = subscription.tier;
        this.tier = tier;
    }

    public void setTier(int tier){this.tier = tier;}


    public boolean isDggChatFlag() {
        return dggChatFlag;
    }

    public boolean isDggDebugFlag() {
        return dggChatFlag;
    }

    public void setDGGChatFlag(boolean b) {
        dggChatFlag = b;
    }

    public void setDGGDebugFlag(boolean b) {
        dggDebugFlag = b;
    }
}
