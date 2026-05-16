package com.arkflame.smpmenus.util;

import org.bukkit.Sound;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class SoundResolverTest {
    @Test
    public void resolvesLegacyButtonClickAlias() {
        Assertions.assertTrue(SoundResolver.resolve("UI_BUTTON_CLICK").isPresent());
    }

    @Test
    public void returnsEmptyForUnknownSound() {
        Assertions.assertFalse(SoundResolver.resolve("NO_SUCH_SOUND_FOR_TEST").isPresent());
    }
}