package pepe.wins.DGGServerPlugin.lib.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DGGChat {

    private final DGGChatBus bus;
    private final String url;
    private final String origin;
    private final String authToken;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);

    private volatile WebSocketClient client;
    private volatile boolean shuttingDown = false;

    private volatile int reconnectAttempts = 0;

    public DGGChat(String url, String origin, String authToken, DGGChatBus bus) {
        this.url = url;
        this.origin = origin;
        this.authToken = authToken;
        this.bus = bus;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "DGGChat-Reconnect");
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Starts (or restarts) the websocket connection.
     * Safe to call multiple times.
     */
    public synchronized void connect() {
        if (shuttingDown) {
            System.out.println("[DGGChat] connect() ignored: shutting down.");
            return;
        }

        WebSocketClient c = this.client;
        if (c != null && !c.isClosed()) {
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

            final WebSocketClient newClient = new WebSocketClient(new URI(url), headers) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("[DGGChat] OPEN");
                    reconnectAttempts = 0;
                    reconnectScheduled.set(false);
                    bus.fireOpen();
                }

                @Override
                public void onMessage(String message) {
                    bus.handleRawLine(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[DGGChat] CLOSE code=" + code + " remote=" + remote + " reason=" + reason);
                    bus.fireClose(code, reason, remote);

                    if (!shuttingDown) {
                        scheduleReconnect("close");
                    }
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("[DGGChat] ERROR: " + ex);
                    bus.fireError(ex);

                    if (!shuttingDown) {
                        scheduleReconnect("error");
                    }
                }
            };

            this.client = newClient;

            Thread t = new Thread(() -> {
                try {
                    System.out.println("[DGGChat] Connecting to " + url + " ...");
                    newClient.connectBlocking();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[DGGChat] connect thread interrupted.");
                    if (!shuttingDown) scheduleReconnect("interrupted");
                } catch (Exception e) {
                    System.out.println("[DGGChat] connect failed: " + e);
                    e.printStackTrace();
                    if (!shuttingDown) scheduleReconnect("connect failed");
                }
            }, "DGGChat-Connect");
            t.setDaemon(true);
            t.start();

        } catch (Exception e) {
            System.out.println("[DGGChat] Failed to create client: " + e);
            e.printStackTrace();
            scheduleReconnect("create client failed");
        }
    }

    /**
     * Explicit shutdown: will NOT attempt to reconnect.
     * Call this from plugin onDisable().
     */
    public synchronized void disconnect() {
        shuttingDown = true;

        WebSocketClient c = this.client;
        this.client = null;

        try {
            if (c != null) c.close();
        } catch (Exception ignored) {}

        scheduler.shutdownNow();
        System.out.println("[DGGChat] Disconnected (shutdown).");
    }

    public boolean isOpen() {
        WebSocketClient c = this.client;
        return c != null && c.isOpen();
    }

    private void scheduleReconnect(String why) {
        if (shuttingDown) return;

        // Only schedule one reconnect at a time.
        if (!reconnectScheduled.compareAndSet(false, true)) {
            return;
        }

        reconnectAttempts++;
        long delaySeconds = 5;

        System.out.println("[DGGChat] Reconnect scheduled in " + delaySeconds + "s (why=" + why +
                ", attempt=" + reconnectAttempts + ")");

        scheduler.schedule(() -> {
            try {
                reconnectScheduled.set(false);

                if (shuttingDown) return;

                // Make sure any old client is closed before reconnecting.
                WebSocketClient c = this.client;
                if (c != null) {
                    try { c.close(); } catch (Exception ignored) {}
                }
                this.client = null;

                connect();
            } catch (Exception e) {
                System.out.println("[DGGChat] Reconnect attempt crashed: " + e);
                e.printStackTrace();

                if (!shuttingDown) {
                    reconnectScheduled.set(false);
                    scheduleReconnect("reconnect crash");
                }
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }
}
