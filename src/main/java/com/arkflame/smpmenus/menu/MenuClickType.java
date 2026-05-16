package com.arkflame.smpmenus.menu;

import org.bukkit.event.inventory.ClickType;

public enum MenuClickType {
    ANY,
    LEFT,
    RIGHT,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    MIDDLE,
    DROP;

    public static MenuClickType from(final ClickType clickType) {
        if (clickType == null) {
            return ANY;
        }
        if (clickType.isLeftClick() && clickType.isShiftClick()) {
            return SHIFT_LEFT;
        }
        if (clickType.isRightClick() && clickType.isShiftClick()) {
            return SHIFT_RIGHT;
        }
        if (clickType.isLeftClick()) {
            return LEFT;
        }
        if (clickType.isRightClick()) {
            return RIGHT;
        }
        if (clickType == ClickType.MIDDLE) {
            return MIDDLE;
        }
        if (clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {
            return DROP;
        }
        return ANY;
    }
}
