package pepe.wins.DGGServerPlugin.lib.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import pepe.wins.DGGServerPlugin.DGGServerPlugin;
import pepe.wins.DGGServerPlugin.DggPlayer;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class Database {
    private final HikariDataSource hikari;
    private final FileConfiguration config;
    private final Logger log;

    public Database(DGGServerPlugin plugin) {
        this.config = plugin.getConfig();
        this.log = plugin.getLogger();
        this.hikari = buildDataSource();
        createPlayersTable();
    }

    private HikariDataSource buildDataSource() {
        String address  = mustGet("db.address");
        String portStr  = mustGet("db.port");
        String database = mustGet("db.name");
        String username = mustGet("db.user");
        String password = mustGet("db.pass");

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("db.port must be an integer, got: " + portStr, e);
        }

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MariaDB JDBC driver not found.",
                    e
            );
        }

        log.info("[db] Initializing MariaDB pool for " + address + ":" + port + "/" + database);

        HikariConfig hc = new HikariConfig();
        hc.setPoolName("DGGServerPlugin-MariaDB");
        hc.setMaximumPoolSize(10);
        hc.setMaxLifetime(600_000);
        hc.setConnectionTimeout(10_000);
        hc.setLeakDetectionThreshold(0);

        hc.setJdbcUrl("jdbc:mariadb://" + address + ":" + port + "/" + database);

        hc.setUsername(username);
        hc.setPassword(password);
        hc.addDataSourceProperty("useUnicode", "true");
        hc.addDataSourceProperty("characterEncoding", "utf8mb4");

        return new HikariDataSource(hc);
    }

    private String mustGet(String path) {
        String v = config.getString(path);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing/empty config value: " + path);
        }
        return v;
    }

    public HikariDataSource getHikariDataSource() {
        return hikari;
    }

    /**
     * Stores DGG player identity + metadata
     *
     * Columns:
     * - uuid (PK)
     * - dgg_id
     * - nick
     * - roles_json
     * - features_json
     * - subscription
     * - updated_at
     */
    public void createPlayersTable() {
        final String sql =
                "CREATE TABLE IF NOT EXISTS Players (" +
                        "  uuid VARCHAR(36) NOT NULL," +
                        "  dgg_id BIGINT NULL," +
                        "  nick VARCHAR(64) NULL," +
                        "  roles_json TEXT NULL," +
                        "  features_json TEXT NULL," +
                        "  subscription INT NULL," +
                        "  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "  PRIMARY KEY (uuid)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {

            log.fine("Executing SQL: " + sql);
            statement.execute(sql);
        } catch (SQLException e) {
            log.warning("[db] Could not create/find Players table: " + e.getMessage());
        }
    }

    public void savePlayer(DggPlayer player) {
        if (player == null) return;

        final UUID uuid = player.uuid();
        if (uuid == null) return;

        final String upsert =
                "INSERT INTO Players (uuid, dgg_id, nick, roles_json, features_json, subscription) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "dgg_id=VALUES(dgg_id), " +
                        "nick=VALUES(nick), " +
                        "roles_json=VALUES(roles_json), " +
                        "features_json=VALUES(features_json), " +
                        "subscription=VALUES(subscription);";

        try (Connection connection = hikari.getConnection();
             PreparedStatement p = connection.prepareStatement(upsert)) {

            p.setString(1, uuid.toString());

            long dggId = player.dggId();
            if (dggId <= 0) p.setNull(2, Types.BIGINT);
            else p.setLong(2, dggId);

            p.setString(3, nullIfBlank(player.nick()));
            p.setString(4, toJsonArray(player.roles()));
            p.setString(5, toJsonArray(player.features()));
            p.setInt(6, player.subscription());

            log.fine("Executing SQL: " + p);
            p.executeUpdate();

        } catch (SQLException e) {
            log.warning("[db] savePlayer failed: " + e.getMessage());
        }
    }

    private static String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // Minimal JSON array encoder (no deps). Stores ["a","b"].
    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (String item : list) {
            if (item == null) continue;
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(jsonEscape(item)).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static List<String> fromJsonArray(String json) {
        if (json == null) return List.of();
        String s = json.trim();
        if (s.isEmpty() || s.equals("[]")) return List.of();

        // Minimal decoder for ["a","b"] that matches your encoder (no nested/escaped quotes beyond \")
        List<String> out = new java.util.ArrayList<>();
        int i = 0;
        if (s.charAt(i) != '[') return List.of();
        i++;

        while (i < s.length()) {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if (i < s.length() && s.charAt(i) == ']') break;

            if (i >= s.length() || s.charAt(i) != '"') break;
            i++; // skip opening quote

            StringBuilder item = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\' && i < s.length()) {
                    char esc = s.charAt(i++);
                    switch (esc) {
                        case '"': item.append('"'); break;
                        case '\\': item.append('\\'); break;
                        case 'b': item.append('\b'); break;
                        case 'f': item.append('\f'); break;
                        case 'n': item.append('\n'); break;
                        case 'r': item.append('\r'); break;
                        case 't': item.append('\t'); break;
                        case 'u':
                            if (i + 4 <= s.length()) {
                                String hex = s.substring(i, i + 4);
                                i += 4;
                                try { item.append((char) Integer.parseInt(hex, 16)); }
                                catch (NumberFormatException ignored) {}
                            }
                            break;
                        default: item.append(esc); break;
                    }
                } else {
                    item.append(c);
                }
            }
            out.add(item.toString());

            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            if (i < s.length() && s.charAt(i) == ',') i++;
        }

        return out;
    }

    public DggPlayer loadPlayer(UUID uuid) {
        if (uuid == null) return null;

        final String sql =
                "SELECT uuid, dgg_id, nick, roles_json, features_json, subscription " +
                        "FROM Players WHERE uuid=? LIMIT 1;";

        try (Connection connection = hikari.getConnection();
             PreparedStatement p = connection.prepareStatement(sql)) {

            p.setString(1, uuid.toString());
            log.fine("Executing SQL: " + p);

            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                DggPlayer player = new DggPlayer(uuid);

                long dggId = rs.getLong("dgg_id");
                if (!rs.wasNull()) {
                    player.setDggId(dggId);
                }

                String nick = rs.getString("nick");
                if (nick != null) player.setNick(nick);
                player.setRoles(fromJsonArray(rs.getString("roles_json")));
                player.setFeatures(fromJsonArray(rs.getString("features_json")));

                int sub = rs.getInt("subscription");
                player.setSubscription(sub);
                return player;
            }

        } catch (SQLException e) {
            log.warning("[db] loadPlayer failed: " + e.getMessage());
            return null;
        }
    }

    public void close() {
        hikari.close();
    }
}

