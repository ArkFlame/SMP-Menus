package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

@RequiredArgsConstructor
public final class MenuCloseListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            plugin.getMenuManager().onClose((Player) event.getPlayer());
        }
    }
}
