package pepe.wins.DGGServerPlugin.commands.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatBus;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatListener;
import pepe.wins.DGGServerPlugin.lib.chat.DGGMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SyncSubcommand implements Subcommand {

    /** Let the rest of your plugin react to a successful sync. */
    @FunctionalInterface
    public interface SyncHandler {
        void onSynced(Player player, String dggNick);
    }

    private static final long TTL_MS = 10 * 60 * 1000; // 10 minutes

    private final DGGChatBus bus;

    private final DggPlayerManager playerManager;

    private final Set<String> seenNicks = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Map<UUID, PendingSync> pending = new ConcurrentHashMap<>();

    private record PendingSync(String requestedNick, int code, long expiresAtMs) {}

    public SyncSubcommand(DGGChatBus bus, DggPlayerManager playerManager) {
        this.bus = bus;
        this.playerManager = playerManager;

        bus.addListener(new DGGChatListener() {
            @Override
            public void onMsg(DGGMessage msg) {
                if (msg == null) return;
                if (msg.nick != null && !msg.nick.isBlank()) {
                    seenNicks.add(msg.nick);
                }
                checkSyncMatch(msg);
            }
        });
    }

    @Override
    public String name() { return "sync"; }

    @Override
    public String permission() { return "dgg.command.sync"; }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player caller)) {
            sender.sendMessage(Component.text("Players only.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            caller.sendMessage(Component.text("Usage: /dgg sync <dgg-username>", NamedTextColor.RED));
            return true;
        }

        String reqName = args[0].trim();
        if (reqName.isEmpty()) {
            caller.sendMessage(Component.text("Please provide a DGG username.", NamedTextColor.RED));
            return true;
        }

        int reqNum = (int) (Math.random() * 10000);
        long expiresAt = System.currentTimeMillis() + TTL_MS;

        pending.put(caller.getUniqueId(), new PendingSync(reqName, reqNum, expiresAt));

        caller.sendMessage(Component.text("To verify, type exactly:", NamedTextColor.GRAY));
        caller.sendMessage(Component.text(reqName + " " + reqNum, NamedTextColor.YELLOW));
        caller.sendMessage(Component.text("in DGG chat as " + reqName, NamedTextColor.GRAY));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            synchronized (seenNicks) {
                return new ArrayList<>(seenNicks);
            }
        }
        return List.of();
    }

    private void checkSyncMatch(DGGMessage msg) {
        long now = System.currentTimeMillis();
        if (msg.nick == null || msg.data == null) return;

        String msgNick = msg.nick.trim();
        String msgText = msg.data.trim();

        if (pending.isEmpty()) return;

        for (var entry : pending.entrySet()) {
            UUID uuid = entry.getKey();
            PendingSync ps = entry.getValue();

            if (ps.expiresAtMs <= now) {
                pending.remove(uuid);
                continue;
            }

            if (!msgNick.equalsIgnoreCase(ps.requestedNick)) {
                continue;
            }

            String[] parts = msgText.split("\\s+");
            if (parts.length < 2) continue;

            String typedName = parts[0];
            String typedCodeStr = parts[1];

            if (!typedName.equalsIgnoreCase(ps.requestedNick)) continue;

            int typedCode;
            try {
                typedCode = Integer.parseInt(typedCodeStr);
            } catch (NumberFormatException ignored) {
                continue;
            }

            if (typedCode != ps.code) continue;

            pending.remove(uuid);

            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(Component.text("DGG sync verified for " + ps.requestedNick, NamedTextColor.GREEN));
            }

            playerManager.get(uuid)
                    .thenAccept(dp -> {
                        dp.setDggId(msg.id);
                        playerManager.reindex(uuid);
                        playerManager.save(uuid);
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }
}
