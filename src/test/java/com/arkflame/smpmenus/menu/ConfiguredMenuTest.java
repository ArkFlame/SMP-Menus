package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementGroup;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ConfiguredMenuTest {
    @Test
    public void indexesItemsBySlot() {
        final MenuItemDefinition item = new MenuItemDefinition(
                "test",
                "CHEST",
                null,
                0,
                0,
                "",
                1,
                "&fTest",
                Collections.<String>emptyList(),
                Collections.singletonList(3),
                0,
                false,
                false,
                true,
                false,
                "",
                RequirementGroup.empty(),
                new EnumMap<MenuClickType, RequirementGroup>(MenuClickType.class),
                new EnumMap<MenuClickType, java.util.List<String>>(MenuClickType.class)
        );
        final ConfiguredMenu menu = new ConfiguredMenu(
                "help",
                true,
                "",
                "&8Help",
                9,
                Collections.<String>emptyList(),
                true,
                0,
                false,
                0L,
                Collections.<String>emptyList(),
                Collections.singletonList(item)
        );
        Assertions.assertEquals(1, menu.getItemsForSlot(3).size());
    }

    @Test
    public void normalizesNegativeClickCooldown() {
        final ConfiguredMenu menu = new ConfiguredMenu(
                "help",
                true,
                "",
                "&8Help",
                9,
                Collections.<String>emptyList(),
                true,
                0,
                false,
                -1L,
                Collections.<String>emptyList(),
                Collections.<MenuItemDefinition>emptyList()
        );
        Assertions.assertEquals(0L, menu.getClickCooldownMillis());
    }

    @Test
    public void normalizesBlankMenuPermission() {
        final ConfiguredMenu menu = new ConfiguredMenu(
                "help",
                true,
                "  ",
                "&8Help",
                9,
                Collections.<String>emptyList(),
                true,
                0,
                false,
                0L,
                Collections.<String>emptyList(),
                Collections.<MenuItemDefinition>emptyList()
        );
        Assertions.assertEquals("", menu.getPermission());
    }
}
