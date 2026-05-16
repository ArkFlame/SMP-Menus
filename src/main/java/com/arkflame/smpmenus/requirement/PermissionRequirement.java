package com.arkflame.smpmenus.requirement;

import org.bukkit.entity.Player;

public final class PermissionRequirement implements Requirement {
    private final String permission;
    private final boolean expected;

    public PermissionRequirement(final String permission, final boolean expected) {
        this.permission = permission;
        this.expected = expected;
    }

    @Override
    public boolean passes(final Player player, final String menuId) {
        return player != null && player.hasPermission(permission) == expected;
    }
}
