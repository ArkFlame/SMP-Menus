package com.arkflame.smpmenus.hook;

import com.arkflame.smpmenus.util.ReflectionUtil;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PlaceholderHook {
    private final Plugin plugin;
    private final boolean enabled;
    private final Method setPlaceholdersMethod;

    public PlaceholderHook(final Plugin plugin) {
        this.plugin = plugin;
        Method method = null;
        boolean hookEnabled = false;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            final Class<?> placeholderApi = ReflectionUtil.getClass("me.clip.placeholderapi.PlaceholderAPI");
            if (placeholderApi != null) {
                method = ReflectionUtil.getMethod(placeholderApi, "setPlaceholders", Player.class, String.class);
                hookEnabled = method != null;
            }
        }
        this.setPlaceholdersMethod = method;
        this.enabled = hookEnabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String apply(final Player player, final String value) {
        if (!enabled || player == null || value == null) {
            return value == null ? "" : value;
        }
        try {
            return String.valueOf(setPlaceholdersMethod.invoke(null, player, value));
        } catch (final ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "PlaceholderAPI placeholder expansion failed.", exception);
            return value;
        }
    }
}
