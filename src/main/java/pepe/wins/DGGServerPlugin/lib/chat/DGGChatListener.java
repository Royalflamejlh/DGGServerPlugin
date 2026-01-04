package pepe.wins.DGGServerPlugin.lib.chat;

public interface DGGChatListener {
    default void onOpen() {}
    default void onClose(int code, String reason, boolean remote) {}
    default void onError(Throwable t) {}

    default void onRaw(String raw) {}

    default void onMsg(DGGMessage msg) {}
    default void onErr(String err) {}
    default void onPollStart(String jsonOrPayload) {}
    default void onPollStop(String jsonOrPayload) {}
}
