package com.arkflame.smpmenus.head;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class HeadMaterialTokenTest {
    @Test
    public void detectsNamedHeadToken() {
        Assertions.assertTrue(HeadItemFactory.isHeadToken("head-Notch"));
    }

    @Test
    public void detectsBaseHeadToken() {
        Assertions.assertTrue(HeadItemFactory.isHeadToken("basehead-abc"));
    }

    @Test
    public void detectsTextureToken() {
        Assertions.assertTrue(HeadItemFactory.isHeadToken("texture-abc"));
    }

    @Test
    public void ignoresNormalMaterial() {
        Assertions.assertFalse(HeadItemFactory.isHeadToken("STONE"));
    }
}