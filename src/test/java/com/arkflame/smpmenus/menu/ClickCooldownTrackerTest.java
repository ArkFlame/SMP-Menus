package com.arkflame.smpmenus.menu;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ClickCooldownTrackerTest {
    @Test
    public void firstClickPasses() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        Assertions.assertTrue(tracker.tryAcquire(UUID.randomUUID(), "help", 1000L, 500L));
    }

    @Test
    public void secondClickBeforeExpiryFails() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        final UUID playerId = UUID.randomUUID();
        tracker.tryAcquire(playerId, "help", 1000L, 500L);
        Assertions.assertFalse(tracker.tryAcquire(playerId, "help", 1200L, 500L));
    }

    @Test
    public void clickAtExpiryPasses() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        final UUID playerId = UUID.randomUUID();
        tracker.tryAcquire(playerId, "help", 1000L, 500L);
        Assertions.assertTrue(tracker.tryAcquire(playerId, "help", 1500L, 500L));
    }

    @Test
    public void differentMenuIsIndependent() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        final UUID playerId = UUID.randomUUID();
        tracker.tryAcquire(playerId, "help", 1000L, 500L);
        Assertions.assertTrue(tracker.tryAcquire(playerId, "rules", 1200L, 500L));
    }

    @Test
    public void zeroCooldownNeverBlocks() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        final UUID playerId = UUID.randomUUID();
        tracker.tryAcquire(playerId, "help", 1000L, 0L);
        Assertions.assertTrue(tracker.tryAcquire(playerId, "help", 1000L, 0L));
    }

    @Test
    public void clearRemovesPlayerCooldown() {
        final ClickCooldownTracker tracker = new ClickCooldownTracker();
        final UUID playerId = UUID.randomUUID();
        tracker.tryAcquire(playerId, "help", 1000L, 500L);
        tracker.clear(playerId);
        Assertions.assertTrue(tracker.tryAcquire(playerId, "help", 1200L, 500L));
    }
}