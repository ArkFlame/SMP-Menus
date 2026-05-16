package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public final class PlayerQuitListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        plugin.getMenuManager().onQuit(event.getPlayer());
    }
}