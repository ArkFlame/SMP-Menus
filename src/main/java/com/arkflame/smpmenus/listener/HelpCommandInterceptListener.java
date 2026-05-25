package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import com.arkflame.smpmenus.menu.MenuOpenContext;
import com.arkflame.smpmenus.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@RequiredArgsConstructor
public final class HelpCommandInterceptListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent event) {
        if (!plugin.getSettings().isInterceptMenuOpenCommands()) {
            return;
        }
        final String message = event.getMessage();
        final String first = StringUtil.firstToken(message);
        if (first.equalsIgnoreCase("smpmenus") || first.equalsIgnoreCase("smpmenusadmin") || first.equalsIgnoreCase("smpm") || first.equalsIgnoreCase("smphelp") || first.equalsIgnoreCase("smphelpadmin") || first.equalsIgnoreCase("smph")) {
            return;
        }
        if (!plugin.getSettings().isInterceptHelpCommand() && first.equalsIgnoreCase("help")) {
            return;
        }
        if (plugin.getCommandRegistry() != null && plugin.getCommandRegistry().isExactRegisteredLabel(first)) {
            return;
        }
        final String menuId = plugin.getMenuManager().findMenuByCommand(first);
        if (menuId == null) {
            return;
        }
        event.setCancelled(true);
        plugin.getMenuManager().open(event.getPlayer(), menuId, MenuOpenContext.of(event.getPlayer(), menuId, StringUtil.commandArguments(message)));
    }
}
