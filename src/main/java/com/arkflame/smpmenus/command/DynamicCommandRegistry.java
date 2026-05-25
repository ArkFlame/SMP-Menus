package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import com.arkflame.smpmenus.menu.ConfiguredMenu;
import com.arkflame.smpmenus.util.FoliaAPI;
import com.arkflame.smpmenus.util.StringUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

public final class DynamicCommandRegistry {
    private final SMPMenusPlugin plugin;
    private final SmpMenusCommand adminExecutor;
    private final DynamicMenuTabCompleter tabCompleter;
    private final List<Command> registeredCommands = new ArrayList<Command>();
    private final Set<String> exactRegisteredLabels = new HashSet<String>();
    private CommandMap commandMap;

    public DynamicCommandRegistry(final SMPMenusPlugin plugin, final SmpMenusCommand adminExecutor) {
        this.plugin = plugin;
        this.adminExecutor = adminExecutor;
        this.tabCompleter = new DynamicMenuTabCompleter(plugin);
    }

    public void reload() {
        unregisterAll();
        final CommandMap map = commandMap();
        if (map == null) {
            plugin.getLogger().warning("Failed to obtain CommandMap, cannot register commands.");
            return;
        }
        final DynamicSmpMenusCommand adminCommand = new DynamicSmpMenusCommand(plugin, adminExecutor, tabCompleter);
        register(adminCommand);
        registerMenuCommands();
        refreshCommandTreeForOnlinePlayers();
    }

    public void shutdown() {
        unregisterAll();
    }

    public boolean isExactRegisteredLabel(final String label) {
        return exactRegisteredLabels.contains(label.toLowerCase(Locale.ROOT));
    }

    public void refreshCommandTreeForOnlinePlayers() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            refreshCommandTree(player);
        }
    }

    public void refreshCommandTree(final Player player) {
        if (player == null) {
            return;
        }
        FoliaAPI.runTaskForEntity(player, () -> {
            try {
                final java.lang.reflect.Method updateMethod = player.getClass().getMethod("updateCommands");
                updateMethod.invoke(player);
            } catch (final NoSuchMethodException e) {
                plugin.getLogger().fine("Player.updateCommands not available: " + e.getMessage());
            } catch (final Exception e) {
                plugin.getLogger().fine("Failed to invoke updateCommands: " + e.getMessage());
            }
        });
    }

    private CommandMap commandMap() {
        if (commandMap != null) {
            return commandMap;
        }
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
            }
        } catch (final Exception e) {
            plugin.getLogger().warning("Failed to obtain CommandMap via reflection: " + e.getMessage());
        }
        return commandMap;
    }

    private void register(final Command command) {
        final CommandMap map = commandMap();
        if (map == null) {
            return;
        }
        final String pluginName = plugin.getName().toLowerCase(Locale.ROOT);
        final boolean registered = map.register(pluginName, command);
        registeredCommands.add(command);
        captureExactLabels(command);
        if (!registered) {
            plugin.getLogger().warning("Command registration returned false for label: " + command.getName()
                    + " - may have conflicted with an existing command.");
        }
    }

    private void registerMenuCommands() {
        final List<String> menuIds = new ArrayList<String>(plugin.getMenuManager().getMenuIds());
        for (final String id : menuIds) {
            final ConfiguredMenu menu = plugin.getMenuManager().getMenu(id);
            if (menu == null || !menu.isEnabled() || !menu.isRegisterCommand()) {
                continue;
            }
            final List<String> labels = commandLabelsFor(menu);
            if (labels.isEmpty()) {
                continue;
            }
            final Set<String> reserved = reservedAdminLabels();
            final List<String> filteredLabels = new ArrayList<String>();
            for (final String label : labels) {
                if (reserved.contains(label.toLowerCase(Locale.ROOT))) {
                    plugin.getLogger().warning("Menu '" + id + "' uses reserved admin label '" + label + "', skipping registration.");
                    continue;
                }
                filteredLabels.add(label);
            }
            if (filteredLabels.isEmpty()) {
                continue;
            }
            final String primaryLabel = filteredLabels.get(0);
            final List<String> aliases = filteredLabels.size() > 1 ? filteredLabels.subList(1, filteredLabels.size()) : null;
            final DynamicMenuOpenCommand menuCommand = new DynamicMenuOpenCommand(plugin, primaryLabel, aliases, menu, tabCompleter);
            register(menuCommand);
        }
    }

    private List<String> commandLabelsFor(final ConfiguredMenu menu) {
        final List<String> rawCommands = menu.getOpenCommands();
        if (rawCommands == null || rawCommands.isEmpty()) {
            return new ArrayList<String>();
        }
        final List<String> labels = new ArrayList<String>();
        final Set<String> seen = new HashSet<String>();
        for (final String raw : rawCommands) {
            final String label = normalizeLabel(raw);
            if (label.isEmpty() || label.contains(":")) {
                continue;
            }
            if (seen.add(label)) {
                labels.add(label);
            }
        }
        return labels;
    }

    private static String normalizeLabel(final String raw) {
        final String token = StringUtil.firstToken(raw);
        return token.replace("/", "").toLowerCase(Locale.ROOT).trim();
    }

    private Set<String> reservedAdminLabels() {
        final Set<String> reserved = new HashSet<String>();
        reserved.add("smpmenus");
        reserved.add("smpmenusadmin");
        reserved.add("smpm");
        reserved.add("smphelp");
        reserved.add("smphelpadmin");
        reserved.add("smph");
        return reserved;
    }

    private void unregisterAll() {
        if (commandMap == null) {
            commandMap = commandMap();
        }
        final List<Command> commandsToRemove = new ArrayList<Command>(registeredCommands);
        registeredCommands.clear();
        exactRegisteredLabels.clear();
        for (final Command command : commandsToRemove) {
            try {
                command.unregister(commandMap);
                removeKnownCommandEntries(command);
            } catch (final Exception e) {
                plugin.getLogger().warning("Failed to unregister command " + command.getName() + ": " + e.getMessage());
            }
        }
    }

    private void removeKnownCommandEntries(final Command command) {
        final Map<String, Command> known = knownCommands();
        if (known == null || known.isEmpty()) {
            return;
        }
        final String labelToRemove = command.getName().toLowerCase(Locale.ROOT);
        known.remove(labelToRemove);
        for (final Map.Entry<String, Command> entry : new java.util.HashSet<Map.Entry<String, Command>>(known.entrySet())) {
            if (entry.getValue() == command) {
                known.remove(entry.getKey());
            }
        }
    }

    private Map<String, Command> knownCommands() {
        final CommandMap map = commandMap();
        if (map == null) {
            return java.util.Collections.<String, Command>emptyMap();
        }
        Class<?> clazz = map.getClass();
        while (clazz != null) {
            try {
                final Field knownField = clazz.getDeclaredField("knownCommands");
                knownField.setAccessible(true);
                final Object raw = knownField.get(map);
                if (raw instanceof Map) {
                    return (Map<String, Command>) raw;
                }
            } catch (final NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (final Exception e) {
                plugin.getLogger().fine("Cannot read knownCommands from " + clazz.getSimpleName() + ": " + e.getMessage());
                return java.util.Collections.<String, Command>emptyMap();
            }
        }
        return java.util.Collections.<String, Command>emptyMap();
    }

    private void captureExactLabels(final Command command) {
        final Map<String, Command> known = knownCommands();
        if (known == null || known.isEmpty()) {
            return;
        }
        for (final Map.Entry<String, Command> entry : known.entrySet()) {
            if (entry.getValue() == command && entry.getKey() != null && !entry.getKey().contains(":")) {
                exactRegisteredLabels.add(entry.getKey().toLowerCase(Locale.ROOT));
            }
        }
    }

    private void invokeUpdateCommands(final Player player) {
        if (player == null) {
            return;
        }
        try {
            final java.lang.reflect.Method method = player.getClass().getMethod("updateCommands");
            method.invoke(player);
        } catch (final NoSuchMethodException e) {
                plugin.getLogger().fine("updateCommands not found: " + e.getMessage());
            } catch (final Exception e) {
                plugin.getLogger().fine("updateCommands invocation failed: " + e.getMessage());
        }
    }
}