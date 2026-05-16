package com.arkflame.smpmenus.requirement;

import org.bukkit.entity.Player;

public interface Requirement {
    boolean passes(Player player, String menuId);
}
