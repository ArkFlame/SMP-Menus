package com.arkflame.smpmenus.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
    private final String menuId;

    public MenuHolder(final String menuId) {
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
