package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;

public final class DynamicMenuTabCompleter {
    private final SMPMenusPlugin plugin;
    private final Supplier<Set<String>> menuIdsSupplier;
    private final Supplier<List<String>> playerNamesSupplier;

    public DynamicMenuTabCompleter(final SMPMenusPlugin plugin) {
        this.plugin = plugin;
        this.menuIdsSupplier = null;
        this.playerNamesSupplier = null;
    }

    DynamicMenuTabCompleter(final Supplier<Set<String>> menuIdsSupplier,
            final Supplier<List<String>> playerNamesSupplier) {
        this.plugin = null;
        this.menuIdsSupplier = menuIdsSupplier;
        this.playerNamesSupplier = playerNamesSupplier;
    }

    public List<String> completeAdmin(final CommandSender sender, final String[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return filter(args[0], adminSubcommands());
        }
        if (args.length == 2 && "open".equalsIgnoreCase(args[0])) {
            return filter(args[1], currentMenuIds());
        }
        if (args.length == 3 && "open".equalsIgnoreCase(args[0])) {
            return filter(args[2], onlinePlayerNames());
        }
        return Collections.emptyList();
    }

    public List<String> completeMenuOpen(final CommandSender sender, final String[] args) {
        return Collections.emptyList();
    }

    static List<String> filter(final String prefix, final Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
        if (prefix == null || prefix.isEmpty()) {
            return sorted;
        }
        final String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        final List<String> result = new ArrayList<String>();
        for (final String value : sorted) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> adminSubcommands() {
        final List<String> subcommands = new ArrayList<String>();
        subcommands.add("reload");
        subcommands.add("open");
        subcommands.add("list");
        return subcommands;
    }

    private Set<String> currentMenuIds() {
        if (plugin != null) {
            return plugin.getMenuManager().getMenuIds();
        }
        if (menuIdsSupplier != null) {
            final Set<String> ids = menuIdsSupplier.get();
            return ids != null ? ids : Collections.<String>emptySet();
        }
        return Collections.<String>emptySet();
    }

    private List<String> onlinePlayerNames() {
        if (playerNamesSupplier != null) {
            final List<String> names = playerNamesSupplier.get();
            return names != null ? names : Collections.<String>emptyList();
        }
        if (plugin != null) {
            final List<String> names = new ArrayList<String>();
            for (final org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return names;
        }
        return Collections.<String>emptyList();
    }
}