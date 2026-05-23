package com.arkflame.smpmenus.gui;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class RgbColorTest {
    @Test
    public void parsesCommaSeparatedRgb() {
        final Optional<RgbColor> color = RgbColor.parse("38, 192, 210");
        Assertions.assertTrue(color.isPresent());
        Assertions.assertEquals(38, color.get().getRed());
        Assertions.assertEquals(192, color.get().getGreen());
        Assertions.assertEquals(210, color.get().getBlue());
    }

    @Test
    public void rejectsOutOfRangeChannel() {
        Assertions.assertFalse(RgbColor.parse("256, 0, 0").isPresent());
    }

    @Test
    public void rejectsWrongPartCount() {
        Assertions.assertFalse(RgbColor.parse("1, 2").isPresent());
    }
}
