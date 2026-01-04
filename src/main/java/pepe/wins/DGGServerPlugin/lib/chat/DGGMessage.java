package pepe.wins.DGGServerPlugin.lib.chat;

import java.util.List;

public class DGGMessage {
    public long id;
    public String nick;
    public List<String> roles;
    public List<String> features;
    public String createdDate;
    public Watching watching;
    public Subscription subscription;
    public long timestamp;
    public String data; // the chat text

    public static class Watching {
        public String platform;
        public String id;
    }

    public static class Subscription {
        public int tier;
        public String source;
    }
}
