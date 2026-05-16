package com.arkflame.smpmenus.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class StringUtilTest {
    @Test
    public void stripsLeadingSlash() {
        Assertions.assertEquals("help", StringUtil.stripLeadingSlash("/help"));
    }

    @Test
    public void readsCommandFirstToken() {
        Assertions.assertEquals("help", StringUtil.firstToken("/help menu"));
    }

    @Test
    public void commandArgumentsEmptyWhenOnlyCommand() {
        Assertions.assertArrayEquals(new String[0], StringUtil.commandArguments("/rules"));
    }

    @Test
    public void commandArgumentsPreserveArgumentsAfterCommand() {
        Assertions.assertArrayEquals(new String[]{"open", "rules", "LinsaFTW"}, StringUtil.commandArguments("/smpmenus open rules LinsaFTW"));
    }
}
