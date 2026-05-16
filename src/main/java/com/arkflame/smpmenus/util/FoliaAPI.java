package com.arkflame.smpmenus.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public final class FoliaAPI {
    private static final Map<String, Method> cachedMethods = new java.util.concurrent.ConcurrentHashMap<>();
    

    private static Plugin plugin;
    private static BukkitScheduler bS;
    private static Object globalRegionScheduler;
    private static Object regionScheduler;
    private static Object asyncScheduler;
    private static Boolean isFolia;

    

    private FoliaAPI() {
    }

    public static void init(final Plugin owner) {
        plugin = Objects.requireNonNull(owner, "owner");
        isFolia = null;
        cachedMethods.clear();
        bS = null;
        globalRegionScheduler = null;
        regionScheduler = null;
        asyncScheduler = null;
        isFolia();
    }

    private static Plugin plugin() {
        if (plugin == null) {
            throw new IllegalStateException("FoliaAPI not initialized");
        }
        return plugin;
    }

    

    private static BukkitScheduler getBukkitSchedulerSafe() {
        if (bS != null) {
            return bS;
        }
        if (Bukkit.getServer() == null) {
            return null;
        }
        bS = Bukkit.getScheduler();
        return bS;
    }

    private static boolean determineFolia() {
        final Class<?> regionizedServerClass = ReflectionUtil.getClass("io.papermc.paper.threadedregions.RegionizedServer");
        return regionizedServerClass != null
                && getGlobalRegionSchedulerSafe() != null
                && getRegionSchedulerSafe() != null
                && getAsyncSchedulerSafe() != null;
    }

    private static Object getGlobalRegionSchedulerSafe() {
        if (globalRegionScheduler != null) {
            return globalRegionScheduler;
        }
        if (Bukkit.getServer() == null) {
            return null;
        }
        final Object scheduler = getGlobalRegionScheduler();
        if (scheduler != null) {
            globalRegionScheduler = scheduler;
        }
        return globalRegionScheduler;
    }

    private static Object getRegionSchedulerSafe() {
        if (regionScheduler != null) {
            return regionScheduler;
        }
        if (Bukkit.getServer() == null) {
            return null;
        }
        final Object scheduler = getRegionScheduler();
        if (scheduler != null) {
            regionScheduler = scheduler;
        }
        return regionScheduler;
    }

    private static Object getAsyncSchedulerSafe() {
        if (asyncScheduler != null) {
            return asyncScheduler;
        }
        if (Bukkit.getServer() == null) {
            return null;
        }
        final Object scheduler = getAsyncScheduler();
        if (scheduler != null) {
            asyncScheduler = scheduler;
        }
        return asyncScheduler;
    }

    private static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        try {
            final Method method = clazz.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    private static void cacheMethods() {
        if (globalRegionScheduler != null) {
            final Class<?> grsClass = globalRegionScheduler.getClass();
            final Method runAtFixedRateMethod = getMethod(grsClass, "runAtFixedRate", Plugin.class, Consumer.class,
                    long.class, long.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("globalRegionScheduler.runAtFixedRate", runAtFixedRateMethod);
            }
            final Method runMethod = getMethod(grsClass, "run", Plugin.class, Consumer.class);
            if (runMethod != null) {
                cachedMethods.put("globalRegionScheduler.run", runMethod);
            }
            final Method runDelayedMethod = getMethod(grsClass, "runDelayed", Plugin.class, Consumer.class, long.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("globalRegionScheduler.runDelayed", runDelayedMethod);
            }
            final Method cancelTasksMethod = getMethod(grsClass, "cancelTasks", Plugin.class);
            if (cancelTasksMethod != null) {
                cachedMethods.put("globalRegionScheduler.cancelTasks", cancelTasksMethod);
            }
        }
        if (regionScheduler != null) {
            final Class<?> rsClass = regionScheduler.getClass();
            final Method executeMethod = getMethod(rsClass, "execute", Plugin.class, World.class, int.class, int.class,
                    Runnable.class);
            if (executeMethod != null) {
                cachedMethods.put("regionScheduler.execute", executeMethod);
            }
            final Method executeLocationMethod = getMethod(rsClass, "execute", Plugin.class, Location.class, Runnable.class);
            if (executeLocationMethod != null) {
                cachedMethods.put("regionScheduler.executeLocation", executeLocationMethod);
            }
            final Method runAtFixedRateMethod = getMethod(rsClass, "runAtFixedRate", Plugin.class, Location.class,
                    Consumer.class, long.class, long.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("regionScheduler.runAtFixedRate", runAtFixedRateMethod);
            }
            final Method runDelayedMethod = getMethod(rsClass, "runDelayed", Plugin.class, Location.class, Consumer.class,
                    long.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("regionScheduler.runDelayed", runDelayedMethod);
            }
        }
        final Method getSchedulerMethod = getMethod(Entity.class, "getScheduler");
        if (getSchedulerMethod != null) {
            cachedMethods.put("entity.getScheduler", getSchedulerMethod);
        }
        final Method teleportAsyncMethod = getMethod(Player.class, "teleportAsync", Location.class);
        if (teleportAsyncMethod != null) {
            cachedMethods.put("player.teleportAsync", teleportAsyncMethod);
        }
        if (asyncScheduler != null) {
            final Class<?> asClass = asyncScheduler.getClass();
            final Method runNowMethod = getMethod(asClass, "runNow", Plugin.class, Consumer.class);
            if (runNowMethod != null) {
                cachedMethods.put("asyncScheduler.runNow", runNowMethod);
            }
            final Method runDelayedMethod = getMethod(asClass, "runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            if (runDelayedMethod != null) {
                cachedMethods.put("asyncScheduler.runDelayed", runDelayedMethod);
            }
            final Method runAtFixedRateMethod = getMethod(asClass, "runAtFixedRate", Plugin.class, Consumer.class,
                    long.class, long.class, TimeUnit.class);
            if (runAtFixedRateMethod != null) {
                cachedMethods.put("asyncScheduler.runAtFixedRate", runAtFixedRateMethod);
            }
            final Method cancelTasksMethod = getMethod(asClass, "cancelTasks", Plugin.class);
            if (cancelTasksMethod != null) {
                cachedMethods.put("asyncScheduler.cancelTasks", cancelTasksMethod);
            }
        }
        final Method isOwnedByCurrentRegionMethod = getMethod(Server.class, "isOwnedByCurrentRegion", Location.class);
        if (isOwnedByCurrentRegionMethod != null) {
            cachedMethods.put("server.isOwnedByCurrentRegionLocation", isOwnedByCurrentRegionMethod);
        }
    }

    private static Object invokeMethod(final Method method, final Object object, final Object... args) {
        try {
            if (method != null && object != null) {
                return method.invoke(object, args);
            }
        } catch (final ReflectiveOperationException | RuntimeException exception) {
            final Plugin owner = plugin;
            if (owner != null) {
                owner.getLogger().log(Level.FINE, "FoliaAPI reflection invocation failed.", exception);
            }
        }
        return null;
    }

    private static Object getGlobalRegionScheduler() {
        final Method method = getMethod(Server.class, "getGlobalRegionScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    private static Object getRegionScheduler() {
        final Method method = getMethod(Server.class, "getRegionScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    private static Object getAsyncScheduler() {
        final Method method = getMethod(Server.class, "getAsyncScheduler");
        return invokeMethod(method, Bukkit.getServer());
    }

    public static boolean isFolia() {
        if (isFolia != null) {
            return isFolia.booleanValue();
        }
        isFolia = Boolean.valueOf(determineFolia());
        if (isFolia.booleanValue()) {
            cacheMethods();
        }
        return isFolia.booleanValue();
    }

    public static boolean isOwnedByCurrentRegion(final Location location) {
        if (!isFolia() || location == null || location.getWorld() == null) {
            return false;
        }
        final Method method = cachedMethods.get("server.isOwnedByCurrentRegionLocation");
        final Object owned = invokeMethod(method, Bukkit.getServer(), location);
        return owned instanceof Boolean && ((Boolean) owned).booleanValue();
    }

    public static <T> T callRegionOwned(final Location location, final Callable<T> task) {
        if (task == null) {
            return null;
        }
        if (!isFolia() || location == null || location.getWorld() == null) {
            return callDirect(task);
        }
        if (isOwnedByCurrentRegion(location)) {
            return callDirect(task);
        }
        final CompletableFuture<T> future = new CompletableFuture<>();
        runTaskForRegion(location, () -> {
            try {
                future.complete(task.call());
            } catch (final Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        try {
            return future.get(3L, TimeUnit.SECONDS);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for region-owned task at " + formatLocation(location), exception);
        } catch (final TimeoutException | ExecutionException exception) {
            throw new RuntimeException("Failed region-owned task at " + formatLocation(location), exception);
        }
    }

    private static <T> T callDirect(final Callable<T> task) {
        try {
            return task.call();
        } catch (final RuntimeException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static String formatLocation(final Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }
        return location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private static long normalizeFoliaInitialDelay(final long delay) {
        return delay <= 0L ? 1L : delay;
    }

    private static long ticksToMillis(final long ticks) {
        if (ticks <= 0L) {
            return 0L;
        }
        return ticks * 50L;
    }

    private static long positiveTicksToMillis(final long ticks) {
        return Math.max(50L, ticksToMillis(ticks <= 0L ? 1L : ticks));
    }

    public static void runTaskAsync(final Runnable run, final long delay) {
        final BukkitScheduler scheduler = getBukkitSchedulerSafe();
        if (!isFolia()) {
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLaterAsynchronously(plugin(), run, delay);
            return;
        }
        final Object asyncScheduler = getAsyncSchedulerSafe();
        final Plugin plugin = plugin();
        if (asyncScheduler == null || plugin == null || run == null) {
            return;
        }
        if (delay <= 0L) {
            final Method runNowMethod = cachedMethods.get("asyncScheduler.runNow");
            invokeMethod(runNowMethod, asyncScheduler, plugin, (Consumer<Object>) ignored -> run.run());
            return;
        }
        final Method runDelayedMethod = cachedMethods.get("asyncScheduler.runDelayed");
        invokeMethod(runDelayedMethod, asyncScheduler, plugin, (Consumer<Object>) ignored -> run.run(), ticksToMillis(delay), TimeUnit.MILLISECONDS);
    }

    public static void runTaskAsync(final Runnable run) {
        runTaskAsync(run, 1L);
    }

    public static void runTaskTimerAsync(final Consumer<Object> run, final long delay, final long period) {
        final BukkitScheduler scheduler = getBukkitSchedulerSafe();
        if (!isFolia()) {
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskTimerAsynchronously(plugin(), () -> run.accept(null), delay, period);
            return;
        }
        final Object asyncScheduler = getAsyncSchedulerSafe();
        final Plugin plugin = plugin();
        if (asyncScheduler == null || plugin == null || run == null) {
            return;
        }
        final Method method = cachedMethods.get("asyncScheduler.runAtFixedRate");
        final long initialDelayMs = ticksToMillis(delay <= 0L ? 1L : delay);
        final long periodMs = positiveTicksToMillis(period);
        invokeMethod(method, asyncScheduler, plugin, run, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public static void runTaskTimerAsync(final Runnable runnable, final long delay, final long period) {
        runTaskTimerAsync(obj -> runnable.run(), delay, period);
    }

    public static void runTaskTimer(final Consumer<Object> run, final long delay, final long period) {
        final BukkitScheduler scheduler = getBukkitSchedulerSafe();
        if (!isFolia()) {
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskTimer(plugin(), () -> run.accept(null), delay, period);
            return;
        }
        final Method method = cachedMethods.get("globalRegionScheduler.runAtFixedRate");
        final Object regionSchedulerObj = getGlobalRegionSchedulerSafe();
        final long safeDelay = normalizeFoliaInitialDelay(delay);
        invokeMethod(method, regionSchedulerObj, plugin(), run, safeDelay, period);
    }

    public static void runTask(final Runnable run) {
        final BukkitScheduler scheduler = getBukkitSchedulerSafe();
        if (!isFolia()) {
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), run);
            return;
        }
        final Method method = cachedMethods.get("globalRegionScheduler.run");
        invokeMethod(method, getGlobalRegionSchedulerSafe(), plugin(),
                (Consumer<Object>) ignored -> run.run());
    }

    public static void runTask(final Consumer<Object> run) {
        final BukkitScheduler scheduler = getBukkitSchedulerSafe();
        if (!isFolia()) {
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), () -> run.accept(null));
            return;
        }
        final Method method = cachedMethods.get("globalRegionScheduler.run");
        invokeMethod(method, getGlobalRegionSchedulerSafe(), plugin(), run);
    }

    public static void runTask(final Chunk chunk, final Runnable run) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), run);
            return;
        }
        if (chunk == null) {
            return;
        }
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskLater(final Runnable run, final long delay) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), run, delay);
            return;
        }
        final Method method = cachedMethods.get("globalRegionScheduler.runDelayed");
        invokeMethod(method, getGlobalRegionSchedulerSafe(), plugin(), (Consumer<Object>) ignored -> run.run(),
                delay);
    }

    public static void runTaskLater(final Consumer<Object> run, final long delay) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), () -> run.accept(null), delay);
            return;
        }
        final Method method = cachedMethods.get("globalRegionScheduler.runDelayed");
        invokeMethod(method, getGlobalRegionSchedulerSafe(), plugin(), run, delay);
    }

    public static void runTaskForEntity(final Entity entity, final Runnable run, final Runnable retired, final long delay) {
        if (!isFolia()) {
            if (delay == 0 && Bukkit.isPrimaryThread()) {
                run.run();
                return;
            }
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), run, delay);
            return;
        }
        if (entity == null) {
            return;
        }
        final Method getSchedulerMethod = cachedMethods.get("entity.getScheduler");
        final Object entityScheduler = invokeMethod(getSchedulerMethod, entity);
        if (entityScheduler != null) {
            final String executeKey = "entityScheduler.execute";
            Method executeMethod = cachedMethods.get(executeKey);
            if (executeMethod == null) {
                executeMethod = getMethod(entityScheduler.getClass(), "execute", Plugin.class, Runnable.class,
                        Runnable.class, long.class);
                if (executeMethod != null) {
                    cachedMethods.put(executeKey, executeMethod);
                }
            }
            invokeMethod(executeMethod, entityScheduler, plugin(), run, retired, delay);
        }
    }

    public static void runTaskForEntity(final Entity entity, final Runnable run) {
        runTaskForEntity(entity, run, () -> {
        }, 0L);
    }

    public static Object runTaskForEntityRepeatingHandle(final Entity entity, final Consumer<Object> task, final Runnable retired,
            final long initialDelay, final long period) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return null;
            }
            return scheduler.runTaskTimer(plugin(), () -> task.accept(null), initialDelay,
                    period);
        }
        if (entity == null) {
            return null;
        }
        final Method getSchedulerMethod = cachedMethods.get("entity.getScheduler");
        final Object entityScheduler = invokeMethod(getSchedulerMethod, entity);
        if (entityScheduler != null) {
            final String runAtFixedRateKey = "entityScheduler.runAtFixedRate";
            Method runAtFixedRateMethod = cachedMethods.get(runAtFixedRateKey);
            if (runAtFixedRateMethod == null) {
                runAtFixedRateMethod = getMethod(entityScheduler.getClass(), "runAtFixedRate", Plugin.class,
                        Consumer.class, Runnable.class, long.class, long.class);
                if (runAtFixedRateMethod != null) {
                    cachedMethods.put(runAtFixedRateKey, runAtFixedRateMethod);
                }
            }
            final long safeDelay = normalizeFoliaInitialDelay(initialDelay);
            return invokeMethod(runAtFixedRateMethod, entityScheduler, plugin(), task, retired,
                    safeDelay, period);
        }
        return null;
    }

    public static void runTaskForEntityRepeating(final Entity entity, final Consumer<Object> task, final Runnable retired,
            final long initialDelay, final long period) {
        runTaskForEntityRepeatingHandle(entity, task, retired, initialDelay, period);
    }

    public static void cancelTask(final Object taskHandle) {
        if (taskHandle == null) {
            return;
        }
        if (taskHandle instanceof BukkitTask) {
            ((BukkitTask) taskHandle).cancel();
            return;
        }
        final Method cancelMethod = getMethod(taskHandle.getClass(), "cancel");
        invokeMethod(cancelMethod, taskHandle);
    }

    public static void runTaskForRegion(final World world, final int chunkX, final int chunkZ, final Runnable run) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), run);
            return;
        }
        if (world == null) {
            return;
        }
        if (isOwnedByCurrentRegion(new Location(world, chunkX << 4, 0, chunkZ << 4))) {
            run.run();
            return;
        }
        final Method executeMethod = cachedMethods.get("regionScheduler.execute");
        invokeMethod(executeMethod, getRegionSchedulerSafe(), plugin(), world, chunkX, chunkZ, run);
    }

    public static void runTaskForRegion(final Location location, final Runnable run) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), run);
            return;
        }
        if (location == null) {
            return;
        }
        if (isOwnedByCurrentRegion(location)) {
            run.run();
            return;
        }
        final Method executeMethod = cachedMethods.get("regionScheduler.executeLocation");
        invokeMethod(executeMethod, getRegionSchedulerSafe(), plugin(), location, run);
    }

    public static void runTaskForRegion(final Chunk chunk, final Runnable run) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTask(plugin(), run);
            return;
        }
        if (chunk == null) {
            return;
        }
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskForRegionOrAsync(final Chunk chunk, final Runnable run) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskAsynchronously(plugin(), run);
            return;
        }
        if (chunk == null) {
            return;
        }
        runTaskForRegion(chunk.getWorld(), chunk.getX(), chunk.getZ(), run);
    }

    public static void runTaskForRegionRepeating(final Location location, final Consumer<Object> task, final long initialDelay,
            final long period) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskTimer(plugin(), () -> task.accept(null), initialDelay, period);
            return;
        }
        if (location == null) {
            return;
        }
        final Method runAtFixedRateMethod = cachedMethods.get("regionScheduler.runAtFixedRate");
        final long safeDelay = normalizeFoliaInitialDelay(initialDelay);
        invokeMethod(runAtFixedRateMethod, regionScheduler, plugin(), location, task, safeDelay,
                period);
    }

    public static void runTaskForRegionDelayed(final Location location, final Consumer<Object> task, final long delay) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), () -> task.accept(null), delay);
            return;
        }
        if (location == null) {
            return;
        }
        final Method runDelayedMethod = cachedMethods.get("regionScheduler.runDelayed");
        invokeMethod(runDelayedMethod, regionScheduler, plugin(), location, task, delay);
    }

    public static CompletableFuture<Boolean> teleportPlayer(final Player e, final Location location, final Boolean async, final Runnable complete) {
        if (!isFolia()) {
            FoliaAPI.runTask(() -> {
                e.teleport(location);
                if (complete != null) {
                    complete.run();
                }
            });
            return CompletableFuture.completedFuture(true);
        } else if (async) {
            final Method teleportMethod = cachedMethods.get("player.teleportAsync");
            final boolean invoked = invokeMethod(teleportMethod, e, location) != null;
            if (complete != null) {
                complete.run();
            }
            return CompletableFuture.completedFuture(invoked);
        } else {
            e.teleport(location);
            if (complete != null) {
                complete.run();
            }
            return CompletableFuture.completedFuture(true);
        }
    }

    public static CompletableFuture<Boolean> teleportPlayer(final Player e, final Location location, final Boolean async) {
        return teleportPlayer(e, location, async, null);
    }

    public static void cancelAllTasks() {
        final Plugin owner = plugin;
        if (owner == null) {
            return;
        }
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.cancelTasks(owner);
            return;
        }
        final Method cancelGlobalMethod = cachedMethods.get("globalRegionScheduler.cancelTasks");
        invokeMethod(cancelGlobalMethod, getGlobalRegionSchedulerSafe(), owner);
        final Method cancelAsyncMethod = cachedMethods.get("asyncScheduler.cancelTasks");
        invokeMethod(cancelAsyncMethod, getAsyncSchedulerSafe(), owner);
    }

    public static void runTaskLater(final Location location, final Runnable run, final long delay) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), run, delay);
            return;
        }
        if (location == null) {
            return;
        }
        final Method runDelayedMethod = cachedMethods.get("regionScheduler.runDelayed");
        invokeMethod(
                runDelayedMethod,
                getRegionSchedulerSafe(),
                plugin(),
                location,
                (Consumer<Object>) ignored -> run.run(),
                delay);
    }

    public static void runTaskLater(final Chunk chunk, final Runnable run, final long delay) {
        if (!isFolia()) {
            final BukkitScheduler scheduler = getBukkitSchedulerSafe();
            if (scheduler == null) {
                return;
            }
            scheduler.runTaskLater(plugin(), run, delay);
            return;
        }
        if (chunk == null) {
            return;
        }
        final Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 0, chunk.getZ() << 4);
        runTaskLater(location, run, delay);
    }
}