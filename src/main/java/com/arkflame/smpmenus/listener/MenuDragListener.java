package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public final class MenuDragListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryDrag(final InventoryDragEvent event) {
        final Inventory inventory = event.getInventory();
        if (plugin.getMenuManager().isMenuInventory(inventory)) {
            event.setCancelled(true);
        }
    }
}
