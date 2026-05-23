package com.arkflame.smpmenus.util;

import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class MaterialResolverColorTargetAliasTest {
    @Test
    public void fireworkStarFallsBackToLegacyFireworkChargeOnLegacyApi() {
        Assertions.assertEquals(Material.FIREWORK_CHARGE, MaterialResolver.resolve("FIREWORK_STAR", null, 0, Material.CHEST).getMaterial());
    }

    @Test
    public void splashPotionFallsBackToPotionOnLegacyApi() {
        Assertions.assertEquals(Material.POTION, MaterialResolver.resolve("SPLASH_POTION", null, 0, Material.CHEST).getMaterial());
    }

    @Test
    public void tippedArrowFallsBackToArrowOnLegacyApi() {
        Assertions.assertEquals(Material.ARROW, MaterialResolver.resolve("TIPPED_ARROW", null, 0, Material.CHEST).getMaterial());
    }
}
