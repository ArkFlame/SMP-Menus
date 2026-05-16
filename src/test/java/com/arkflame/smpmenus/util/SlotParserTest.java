package com.arkflame.smpmenus.util;

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class SlotParserTest {
    @Test
    public void parsesRangeSlots() {
        Assertions.assertEquals(Arrays.asList(0, 1, 2), SlotParser.parseSlots(null, Arrays.asList("0-2"), 9));
    }

    @Test
    public void rejectsOutOfInventorySlots() {
        Assertions.assertEquals(Arrays.asList(8), SlotParser.parseSlots(null, Arrays.asList("8-10"), 9));
    }
}
