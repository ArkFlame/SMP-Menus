package com.arkflame.smpmenus.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClickCooldownTracker {
    private final Map<UUID, Map<String, Long>> expiresAtByPlayer = new HashMap<UUID, Map<String, Long>>();

    public boolean tryAcquire(
            final UUID playerId,
            final String menuId,
            final long nowMillis,
            final long cooldownMillis
    ) {
        if (playerId == null || menuId == null || cooldownMillis <= 0L) {
            return true;
        }
        final Map<String, Long> expiresAtByMenu = getOrCreatePlayerMap(playerId);
        final Long expiresAt = expiresAtByMenu.get(menuId);
        if (expiresAt != null && expiresAt.longValue() > nowMillis) {
            return false;
        }
        expiresAtByMenu.put(menuId, safeAdd(nowMillis, cooldownMillis));
        return true;
    }

    public void clear(final UUID playerId) {
        if (playerId != null) {
            expiresAtByPlayer.remove(playerId);
        }
    }

    public void clearAll() {
        expiresAtByPlayer.clear();
    }

    private Map<String, Long> getOrCreatePlayerMap(final UUID playerId) {
        Map<String, Long> expiresAtByMenu = expiresAtByPlayer.get(playerId);
        if (expiresAtByMenu == null) {
            expiresAtByMenu = new HashMap<String, Long>();
            expiresAtByPlayer.put(playerId, expiresAtByMenu);
        }
        return expiresAtByMenu;
    }

    private static long safeAdd(final long left, final long right) {
        final long result = left + right;
        return result < left ? Long.MAX_VALUE : result;
    }
}