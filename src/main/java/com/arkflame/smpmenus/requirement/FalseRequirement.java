package com.arkflame.smpmenus.requirement;

import org.bukkit.entity.Player;

public enum FalseRequirement implements Requirement {
    INSTANCE;

    @Override
    public boolean passes(final Player player, final String menuId) {
        return false;
    }
}
