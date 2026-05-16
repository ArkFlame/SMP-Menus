package com.arkflame.smpmenus.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public final class ConfiguredMenu {
    private final String id;
    private final boolean enabled;
    private final String permission;
    private final String title;
    private final int size;
    private final List<String> openCommands;
    private final boolean registerCommand;
    private final int updateIntervalTicks;
    private final boolean closeOnClick;
    private final long clickCooldownMillis;
    private final List<String> openActions;
    private final List<MenuItemDefinition> items;
    private final Map<Integer, List<MenuItemDefinition>> itemsBySlot;

    public ConfiguredMenu(
            final String id,
            final boolean enabled,
            final String permission,
            final String title,
            final int size,
            final List<String> openCommands,
            final boolean registerCommand,
            final int updateIntervalTicks,
            final boolean closeOnClick,
            final long clickCooldownMillis,
            final List<String> openActions,
            final List<MenuItemDefinition> items
    ) {
        this.id = id;
        this.enabled = enabled;
        this.permission = normalizePermission(permission);
        this.title = title;
        this.size = size;
        this.openCommands = Collections.unmodifiableList(new ArrayList<String>(openCommands));
        this.registerCommand = registerCommand;
        this.updateIntervalTicks = updateIntervalTicks;
        this.closeOnClick = closeOnClick;
        this.clickCooldownMillis = Math.max(0L, clickCooldownMillis);
        this.openActions = Collections.unmodifiableList(new ArrayList<String>(openActions));
        this.items = Collections.unmodifiableList(new ArrayList<MenuItemDefinition>(items));
        this.itemsBySlot = buildItemsBySlot(items);
    }

    private static String normalizePermission(final String permission) {
        return permission == null ? "" : permission.trim();
    }

    private static Map<Integer, List<MenuItemDefinition>> buildItemsBySlot(final List<MenuItemDefinition> allItems) {
        final Map<Integer, List<MenuItemDefinition>> bySlot = new HashMap<Integer, List<MenuItemDefinition>>();
        for (final MenuItemDefinition item : allItems) {
            for (final Integer slot : item.getSlots()) {
                if (slot == null) {
                    continue;
                }
                List<MenuItemDefinition> slotItems = bySlot.get(slot);
                if (slotItems == null) {
                    slotItems = new ArrayList<MenuItemDefinition>();
                    bySlot.put(slot, slotItems);
                }
                slotItems.add(item);
            }
        }
        final Map<Integer, List<MenuItemDefinition>> immutable = new HashMap<Integer, List<MenuItemDefinition>>();
        for (final Map.Entry<Integer, List<MenuItemDefinition>> entry : bySlot.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<MenuItemDefinition>(entry.getValue())));
        }
        return Collections.unmodifiableMap(immutable);
    }

    public List<MenuItemDefinition> getItemsForSlot(final int slot) {
        final List<MenuItemDefinition> slotItems = itemsBySlot.get(slot);
        return slotItems == null ? Collections.<MenuItemDefinition>emptyList() : slotItems;
    }

    public boolean hasPermissionRequirement() {
        return !permission.isEmpty();
    }
}
