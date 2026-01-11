package pepe.wins.DGGServerPlugin.lib.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import pepe.wins.DGGServerPlugin.DggPlayer;

import java.util.List;

public final class ChatStyle {
    private ChatStyle() {}

    // Nice off-white
    private static final TextColor OFFWHITE = TextColor.color(0xE6E6E6);

    // ---------- Public entrypoints ----------

    /** Format a DGG message line (optionally using your cached DggPlayer info if you have it). */
    public static Component line(DGGMessage msg, DggPlayer cachedSender) {
        if (msg == null) return Component.empty();

        Profile p = (cachedSender != null)
                ? Profile.fromPlayer(cachedSender, fallbackNick(msg.nick))
                : Profile.fromDggMessage(msg);

        String text = (msg.data == null) ? "" : msg.data;
        return line(p, text);
    }

    /** Format a Minecraft chat line using a DggPlayer (your canonical source for roles/features/sub tier). */
    public static Component line(DggPlayer sender, String fallbackNick, Component messageComponent) {
        Profile p = (sender != null)
                ? Profile.fromPlayer(sender, fallbackNick(fallbackNick))
                : Profile.fallback(fallbackNick(fallbackNick));

        // Preserve player message text, but we still need the plain text to detect ">"
        String plain = PlainTextComponentSerializer.plainText().serialize(messageComponent);
        boolean quote = startsWithQuote(plain);

        Component pre = prefix(p);
        Component name = styledName(p);

        Component sep = Component.text(": ", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, false);

        Component body;
        if (quote) {
            body = Component.text(plain, NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, false);
        } else {
            // Keep original component, tint if no color, and make sure it's not bold
            body = messageComponent.colorIfAbsent(OFFWHITE)
                    .decoration(TextDecoration.BOLD, false);
        }

        return pre.append(name).append(sep).append(body);
    }

    /** Convenience overload if you already have plain message text. */
    public static Component line(DggPlayer sender, String fallbackNick, String text) {
        Profile p = (sender != null)
                ? Profile.fromPlayer(sender, fallbackNick(fallbackNick))
                : Profile.fallback(fallbackNick(fallbackNick));
        return line(p, text == null ? "" : text);
    }

    // ---------- Name-only entrypoints (tablist / overhead) ----------

    /**
     * Name component for a player (no ": message"), includes role prefix if you want it.
     * Use this for tablist and customName above head.
     */
    public static Component displayName(DggPlayer dp, String fallbackNick) {
        Profile p = (dp != null)
                ? Profile.fromPlayer(dp, fallbackNick(fallbackNick))
                : Profile.fallback(fallbackNick(fallbackNick));

        return prefix(p).append(styledName(p));
    }

    /**
     * Like displayName() but WITHOUT any role prefix.
     * Use this if you truly want "just the nick", no tags.
     */
    public static Component nickOnly(DggPlayer dp, String fallbackNick) {
        Profile p = (dp != null)
                ? Profile.fromPlayer(dp, fallbackNick(fallbackNick))
                : Profile.fallback(fallbackNick(fallbackNick));
        return styledName(p);
    }

    /**
     * A sane nametag color for scoreboard-team usage.
     * (Scoreboard team color can't do per-character rainbow; for tier 5 we pick a fallback color.)
     */
    public static NamedTextColor nametagColor(DggPlayer dp) {
        int tier = (dp == null) ? 0 : dp.subscription();
        return switch (tier) {
            case 1 -> NamedTextColor.BLUE;
            case 2 -> NamedTextColor.AQUA;
            case 3 -> NamedTextColor.GREEN;
            case 4 -> NamedTextColor.LIGHT_PURPLE;
            case 5 -> NamedTextColor.WHITE; // scoreboard can't rainbow name itself
            default -> NamedTextColor.WHITE;
        };
    }

    // ---------- Core formatter ----------

    private static Component line(Profile p, String text) {
        Component pre = prefix(p);
        Component name = styledName(p);

        Component sep = Component.text(": ", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.BOLD, false);

        Component body;
        if (startsWithQuote(text)) {
            body = Component.text(text, NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, false);
        } else {
            body = Component.text(text, OFFWHITE)
                    .decoration(TextDecoration.BOLD, false);
        }

        return pre.append(name).append(sep).append(body);
    }

    // ---------- Styling hooks (roles/features future-proofing) ----------

    /** Prefix based on roles/features. Extend this later. */
    private static Component prefix(Profile p) {
        // Examples — tailor these to your real role names later.
        if (hasRole(p.roles, "admin")) {
            return Component.text("[ADMIN] ", NamedTextColor.RED).decoration(TextDecoration.BOLD, false);
        }
        if (hasRole(p.roles, "mod") || hasRole(p.roles, "moderator")) {
            return Component.text("[MOD] ", NamedTextColor.DARK_GREEN).decoration(TextDecoration.BOLD, false);
        }
        if (hasRole(p.roles, "vip")) {
            return Component.text("[VIP] ", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, false);
        }

        return Component.empty();
    }

    /** Name styling uses subscription tier + feature overrides (like rainbow). */
    private static Component styledName(Profile p) {
        String nick = p.nick == null ? "" : p.nick;

        // Feature override example: if you later decide a feature triggers rainbow regardless of tier.
        boolean rainbow = (p.subscriptionTier == 5) || hasFeature(p.features, "rainbow");

        Component base;
        if (rainbow) {
            base = rainbow(nick, System.currentTimeMillis());
        } else {
            base = switch (p.subscriptionTier) {
                case 1 -> Component.text(nick, NamedTextColor.BLUE);
                case 2 -> Component.text(nick, NamedTextColor.AQUA);
                case 3 -> Component.text(nick, NamedTextColor.GREEN);
                case 4 -> Component.text(nick, NamedTextColor.LIGHT_PURPLE);
                default -> Component.text(nick, NamedTextColor.WHITE);
            };
        }

        // Only name bold
        return base.decorate(TextDecoration.BOLD);
    }

    // ---------- Helpers ----------

    private static boolean hasRole(List<String> roles, String role) {
        if (roles == null || role == null) return false;
        for (String r : roles) {
            if (r != null && r.equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    private static boolean hasFeature(List<String> features, String feature) {
        if (features == null || feature == null) return false;
        for (String f : features) {
            if (f != null && f.equalsIgnoreCase(feature)) return true;
        }
        return false;
    }

    private static String fallbackNick(String nick) {
        return (nick == null || nick.isBlank()) ? "?" : nick;
    }

    private static boolean startsWithQuote(String s) {
        if (s == null) return false;
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i < s.length() && s.charAt(i) == '>';
    }

    /**
     * “Scrolling” rainbow: gradient phase changes with time.
     * Not animated in-place; it shifts each message.
     */
    private static Component rainbow(String text, long nowMs) {
        final float speed = 0.0012f;
        final float step  = 0.085f;

        float baseHue = (nowMs * speed) % 1.0f;

        Component out = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            float hue = (baseHue + i * step) % 1.0f;
            int rgb = hsvToRgb(hue, 1.0f, 1.0f);
            out = out.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(rgb)));
        }
        return out;
    }

    // returns 0xRRGGBB
    private static int hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;

        int i = (int) Math.floor(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float t = v * (1.0f - (1.0f - f) * s);

        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            case 5 -> { r = v; g = p; b = q; }
        }

        int R = Math.round(r * 255.0f);
        int G = Math.round(g * 255.0f);
        int B = Math.round(b * 255.0f);
        return (R << 16) | (G << 8) | B;
    }

    // ---------- Profile (unifies DGGMessage vs DggPlayer data) ----------

    private static final class Profile {
        final String nick;
        final int subscriptionTier;
        final List<String> roles;
        final List<String> features;

        private Profile(String nick, int subscriptionTier, List<String> roles, List<String> features) {
            this.nick = nick;
            this.subscriptionTier = subscriptionTier;
            this.roles = roles;
            this.features = features;
        }

        static Profile fromPlayer(DggPlayer dp, String fallbackNick) {
            String nick = (dp.nick() != null && !dp.nick().isBlank()) ? dp.nick() : fallbackNick;
            return new Profile(nick, dp.subscription(), dp.roles(), dp.features());
        }

        static Profile fromDggMessage(DGGMessage msg) {
            String nick = fallbackNick(msg.nick);
            int tier = (msg.subscription == null) ? 0 : msg.subscription.tier;
            return new Profile(nick, tier, msg.roles, msg.features);
        }

        static Profile fallback(String nick) {
            return new Profile(nick, 0, List.of(), List.of());
        }
    }
}
