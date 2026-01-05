package pepe.wins.DGGServerPlugin.listeners;

import pepe.wins.DGGServerPlugin.DggPlayer;
import pepe.wins.DGGServerPlugin.DggPlayerManager;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatBus;
import pepe.wins.DGGServerPlugin.lib.chat.DGGChatListener;
import pepe.wins.DGGServerPlugin.lib.chat.DGGMessage;

public class DggDataListener implements DGGChatListener {
    private final DggPlayerManager playerManager;

    public DggDataListener(DggPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void onMsg(DGGMessage msg) {
        if (msg == null) return;
        DggPlayer dp = playerManager.get(msg.id);
        if(dp == null) return;
        // todo sync id info
//        dp.setNick(msg.nick);
//        dp.setFeatures(msg.features);
//        dp.setSubscription(msg.subscription.tier);
//        dp.setRoles(msg.roles);

    }
}

