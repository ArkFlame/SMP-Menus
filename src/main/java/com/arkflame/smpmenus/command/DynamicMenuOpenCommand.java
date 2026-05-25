package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import com.arkflame.smpmenus.menu.ConfiguredMenu;
import com.arkflame.smpmenus.menu.MenuOpenContext;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class DynamicMenuOpenCommand extends Command implements PluginIdentifiableCommand {
    private final SMPMenusPlugin plugin;
    private final String menuId;
    private final DynamicMenuTabCompleter tabCompleter;

    DynamicMenuOpenCommand(final SMPMenusPlugin plugin, final String primaryLabel,
            final List<String> aliases, final ConfiguredMenu menu,
            final DynamicMenuTabCompleter tabCompleter) {
        super(primaryLabel);
        this.plugin = plugin;
        this.menuId = menu.getId();
        this.tabCompleter = tabCompleter;
        if (aliases != null && !aliases.isEmpty()) {
            setAliases(aliases);
        }
        setDescription("Opens SMPMenus menu '" + menuId + "'.");
        setUsage("/" + primaryLabel);
        if (menu.hasPermissionRequirement()) {
            setPermission(menu.getPermission());
        }
    }

    @Override
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageService().sendConfigMessage(sender, "only-player", "&cOnly players can use this command.");
            return true;
        }
        final Player player = (Player) sender;
        plugin.getMenuManager().open(player, menuId, MenuOpenContext.of(player, menuId, args));
        return true;
    }

    @Override
    public java.util.List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        final java.util.List<String> result = tabCompleter.completeMenuOpen(sender, args);
        return result != null ? result : java.util.Collections.<String>emptyList();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}