package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class HelpCommand implements CommandExecutor {
    private final SMPMenusPlugin plugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageService().sendConfigMessage(sender, "only-player", "&cOnly players can use this command.");
            return true;
        }
        if (!sender.hasPermission("smpmenus.use") && !sender.hasPermission("smphelp.use")) {
            plugin.getMessageService().sendConfigMessage(sender, "no-permission", "&cYou do not have permission to do that.");
            return true;
        }
        plugin.getMenuManager().openMain((Player) sender);
        return true;
    }
}