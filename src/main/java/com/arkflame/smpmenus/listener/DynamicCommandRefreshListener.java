package com.arkflame.smpmenus.listener;

import com.arkflame.smpmenus.SMPMenusPlugin;
import com.arkflame.smpmenus.util.FoliaAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public final class DynamicCommandRefreshListener implements Listener {
    private final SMPMenusPlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        FoliaAPI.runTaskForEntity(player, new Runnable() {
            @Override
            public void run() {
                if (plugin.getCommandRegistry() != null) {
                    plugin.getCommandRegistry().refreshCommandTree(player);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
            }
        }, 1L);
    }
}