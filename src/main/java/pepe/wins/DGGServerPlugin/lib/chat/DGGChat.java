package pepe.wins.DGGServerPlugin.lib.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DGGChat {
    private final DGGChatBus bus;
    private final String url;
    private final String origin;
    private final String authToken;

    private WebSocketClient client;


    public DGGChat(String url, String origin, String authToken, DGGChatBus bus) {
        this.url = url;
        this.origin = origin;
        this.authToken = authToken;
        this.bus = bus;
    }


    public void connect() {
        if (client != null && !client.isClosed()) {
            System.out.println("[DGGChat] Already connected/connecting.");
            return;
        }

        try {
            Map<String, String> headers = new HashMap<>();
            if (origin != null && !origin.isBlank()) {
                headers.put("Origin", origin);
            }
            if (authToken != null && !authToken.isBlank()) {
                headers.put("Cookie", "authtoken=" + authToken);
            }

            client = new WebSocketClient(new URI(url), headers) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("[DGGChat] OPEN ...");
                    bus.fireOpen();
                }

                @Override
                public void onMessage(String message) {
                    bus.handleRawLine(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[DGGChat] CLOSE ...");
                    bus.fireClose(code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    bus.fireError(ex);
                }
            };

            new Thread(() -> {
                try {
                    System.out.println("[DGGChat] Connecting to " + url + " ...");
                    client.connectBlocking();
                    System.out.println("[DGGChat] Connected.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[DGGChat] connect thread interrupted.");
                } catch (Exception e) {
                    System.out.println("[DGGChat] connect failed: " + e);
                    e.printStackTrace();
                }
            }, "DGGChat-Connect").start();

        } catch (Exception e) {
            System.out.println("[DGGChat] Failed to create client: " + e);
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {}
        }
    }

    public boolean isOpen() {
        return client != null && client.isOpen();
    }
}