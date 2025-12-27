package pepe.wins.DGGServerPlugin;

import java.util.Locale;
import java.util.Optional;

public enum DggTeam {
    JANNIE("jannie"),
    PEPE("pepe"),
    YEE("yee"),
    HOMELESS("homeless");

    private final String id;

    DggTeam(String id) { this.id = id; }

    public String id() { return id; }

    public static Optional<DggTeam> fromArg(String arg) {
        if (arg == null) return Optional.empty();
        String a = arg.toLowerCase(Locale.ROOT);
        for (DggTeam t : values()) {
            if (t.id.equals(a)) return Optional.of(t);
        }
        return Optional.empty();
    }
}
