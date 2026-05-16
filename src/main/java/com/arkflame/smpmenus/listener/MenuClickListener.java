package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import com.arkflame.smpmenus.menu.MenuClickType;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public final class MenuClickListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (!plugin.getMenuManager().isMenuInventory(inventory)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        plugin.getMenuManager().handleClick(player, inventory, event.getRawSlot(), MenuClickType.from(event.getClick()));
    }
}
