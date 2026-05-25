package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SmpMenusCommand implements CommandExecutor {
    private final SMPMenusPlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getMenuManager().openMain((Player) sender);
            } else {
                plugin.getMessageService().send(sender, "&fSMPMenus &7menus: &b" + plugin.getMenuManager().getMenuIds(), null);
            }
            return true;
        }
        final String subcommand = args[0].toLowerCase();
        if ("reload".equals(subcommand)) {
            return reload(sender);
        }
        if ("list".equals(subcommand)) {
            plugin.getMessageService().send(sender, "&#13C3F5&lSMPMenus &8» &fMenus: &b" + plugin.getMenuManager().getMenuIds(), null);
            return true;
        }
        if ("open".equals(subcommand)) {
            return open(sender, args);
        }
        plugin.getMessageService().sendConfigMessage(sender, "unknown-subcommand", "&cUsage: &f/smpmenus reload &7| &f/smpmenus open <menu> [player] &7| &f/smpmenus list");
        return true;
    }

    private static boolean hasAdmin(final CommandSender sender) {
        return sender.hasPermission("smpmenus.admin") || sender.hasPermission("smphelp.admin");
    }

    private boolean reload(final CommandSender sender) {
        if (!hasAdmin(sender)) {
            plugin.getMessageService().sendConfigMessage(sender, "no-permission", "&cYou do not have permission to do that.");
            return true;
        }
        plugin.reloadRuntimeState();
        plugin.getMessageService().sendConfigMessage(sender, "reload", "&#13C3F5&lSMPMenus &8» &fMenus reloaded.");
        if (sender instanceof Player) {
            plugin.getSoundService().playConfigured((Player) sender, "reload");
        }
        return true;
    }

    private boolean open(final CommandSender sender, final String[] args) {
        if (!hasAdmin(sender)) {
            plugin.getMessageService().sendConfigMessage(sender, "no-permission", "&cYou do not have permission to do that.");
            return true;
        }
        if (args.length < 2) {
            plugin.getMessageService().send(sender, "&cUsage: &f/smpmenus open <menu> [player]", null);
            return true;
        }
        final String menu = args[1];
        if (args.length >= 3) {
            final Player target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                plugin.getMessageService().send(sender, plugin.getSettings().getMessage("no-online-player", "&cPlayer not found: &f%player%").replace("%player%", args[2]), null);
                return true;
            }
            plugin.getMenuManager().open(target, menu);
            plugin.getMessageService().send(sender, plugin.getSettings().getMessage("opened-for-other", "&fOpened &b%menu% &ffor &b%player%&f.").replace("%menu%", menu).replace("%player%", target.getName()), null);
            return true;
        }
        if (!(sender instanceof Player)) {
            plugin.getMessageService().sendConfigMessage(sender, "only-player", "&cOnly players can use this command.");
            return true;
        }
        plugin.getMenuManager().open((Player) sender, menu);
        return true;
    }
}