package pepe.wins.DGGServerPlugin.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.lib.chat.ChatStyle;

public final class IngameChatListener implements Listener {
    private final DGGServerPlugin plugin;

    public IngameChatListener(DGGServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var pm = plugin.getPlayerManager();
        DggPlayer dp = pm.getCached(event.getPlayer());

        event.renderer((source, sourceDisplayName, message, viewer) ->
                ChatStyle.line(dp, source.getName(), message)
        );
    }
}
