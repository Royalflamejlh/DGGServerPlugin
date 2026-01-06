package pepe.wins.DGGServerPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pepe.wins.DGGServerPlugin.lib.db.Database;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class DggPlayerManager {
    private final Database db;
    private final DGGServerPlugin plugin;

    // Primary cache
    private final Map<UUID, DggPlayer> cache = new ConcurrentHashMap<>();

    // Secondary index: dggId -> uuid
    private final Map<Long, UUID> byDggId = new ConcurrentHashMap<>();

    // Prevent duplicate concurrent loads
    private final Map<UUID, CompletableFuture<DggPlayer>> inflight = new ConcurrentHashMap<>();

    private static final long AUTOSAVE_PERIOD_TICKS = 12000L;
    private int autosaveTaskId = -1;

    public DggPlayerManager(DGGServerPlugin plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabase();
    }

    /** Call from onEnable() */
    public void startAutosave() {
        if (autosaveTaskId != -1) return;

        autosaveTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::saveAllNow,
                AUTOSAVE_PERIOD_TICKS,
                AUTOSAVE_PERIOD_TICKS
        ).getTaskId();
    }

    /** Call from onDisable() */
    public void stopAutosave() {
        if (autosaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autosaveTaskId);
            autosaveTaskId = -1;
        }
    }

    // --------------------
    // SYNC: cache-only
    // --------------------

    public DggPlayer getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public DggPlayer getCached(Player p) {
        return getCached(p.getUniqueId());
    }

    /** Cache-only lookup by DGG id (returns null if not cached/indexed). */
    public DggPlayer getCachedByDggId(long dggId) {
        UUID uuid = byDggId.get(dggId);
        if (uuid == null) return null;
        return cache.get(uuid);
    }

    /** Optional: returns UUID for a cached DGG id, or null. */
    public UUID getCachedUuidByDggId(long dggId) {
        return byDggId.get(dggId);
    }

    public void remove(UUID uuid) {
        unindex(uuid);
        cache.remove(uuid);
    }

    // --------------------
    // ASYNC: cache -> DB -> create (always returns a player)
    // --------------------

    public CompletableFuture<DggPlayer> get(UUID uuid) {
        DggPlayer cached = cache.get(uuid);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return inflight.computeIfAbsent(uuid, u ->
                runAsync(() -> {
                    DggPlayer again = cache.get(u);
                    if (again != null) return again;

                    DggPlayer dp = db.loadPlayer(u);
                    if (dp == null) dp = new DggPlayer(u);

                    cache.put(u, dp);
                    index(dp);
                    return dp;
                }).whenComplete((dp, err) -> inflight.remove(u))
        );
    }

    public CompletableFuture<DggPlayer> get(Player p) {
        return get(p.getUniqueId());
    }

    // --------------------
    // Index maintenance
    // --------------------

    /**
     * Rebuilds the dggId index entry for this player based on current dp.dggId().
     * Call this after dp.dggId changes.
     */
    public void reindex(UUID uuid) {
        DggPlayer dp = cache.get(uuid);
        if (dp == null) return;

        unindex(uuid);
        index(dp);
    }

    /** Adds/updates dggId -> uuid mapping for this dp (if dp has a valid id). */
    private void index(DggPlayer dp) {
        long id = dp.dggId();
        if (id <= 0) return;
        byDggId.put(id, dp.uuid());
    }

    /** Removes any dggId mapping that points to this uuid. */
    private void unindex(UUID uuid) {
        byDggId.entrySet().removeIf(e -> e.getValue().equals(uuid));
    }

    // --------------------
    // Saves
    // --------------------

    public CompletableFuture<Void> save(DggPlayer dp) {
        return runAsyncVoid(() -> db.savePlayer(dp));
    }

    public CompletableFuture<Void> save(UUID uuid) {
        DggPlayer dp = cache.get(uuid);
        if (dp == null) return CompletableFuture.completedFuture(null);
        return save(dp);
    }

    public CompletableFuture<Void> saveAll() {
        return runAsyncVoid(this::saveAllNow);
    }

    private void saveAllNow() {
        for (DggPlayer dp : cache.values()) {
            db.savePlayer(dp);
        }
    }

    public CompletableFuture<Void> saveAndRemove(UUID uuid) {
        DggPlayer dp = cache.get(uuid);
        if (dp == null) return CompletableFuture.completedFuture(null);

        return save(dp).whenComplete((ok, err) -> remove(uuid));
    }

    // --------------------
    // Internal scheduler helpers
    // --------------------

    private <T> CompletableFuture<T> runAsync(java.util.function.Supplier<T> supplier) {
        CompletableFuture<T> f = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                f.complete(supplier.get());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f;
    }

    private CompletableFuture<Void> runAsyncVoid(Runnable r) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                r.run();
                f.complete(null);
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f;
    }

    public void saveAllBlocking() {
        for (DggPlayer dp : cache.values()) {
            db.savePlayer(dp);
        }
    }
}
