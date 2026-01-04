package pepe.wins.DGGServerPlugin.lib.chat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DGGChatBus {
    private final Plugin plugin;
    private final Gson gson = new Gson();
    private final List<DGGChatListener> listeners = new CopyOnWriteArrayList<>();

    public DGGChatBus(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addListener(DGGChatListener l) { listeners.add(l); }
    public void removeListener(DGGChatListener l) { listeners.remove(l); }

    public void fireOpen() {
        for (var l : listeners) l.onOpen();
    }

    public void fireClose(int code, String reason, boolean remote) {
        for (var l : listeners) l.onClose(code, reason, remote);
    }

    public void fireError(Throwable t) {
        for (var l : listeners) l.onError(t);
    }

    public void handleRawLine(String line) {
        for (var l : listeners) l.onRaw(line);

        if (line == null) return;

        if (line.startsWith("MSG ")) {
            String json = line.substring(4);
            DGGMessage msg;
            try {
                msg = gson.fromJson(json, DGGMessage.class);
            } catch (JsonSyntaxException ex) {
                fireError(ex);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (var l : listeners) l.onMsg(msg);
            });
            return;
        }

        if (line.startsWith("ERR ")) {
            String payload = line.substring(4);
            for (var l : listeners) l.onErr(payload);
            return;
        }

        if (line.startsWith("POLLSTART ")) {
            String payload = line.substring("POLLSTART ".length());
            for (var l : listeners) l.onPollStart(payload);
            return;
        }

        if (line.startsWith("POLLSTOP ")) {
            String payload = line.substring("POLLSTOP ".length());
            for (var l : listeners) l.onPollStop(payload);
        }
    }
}
