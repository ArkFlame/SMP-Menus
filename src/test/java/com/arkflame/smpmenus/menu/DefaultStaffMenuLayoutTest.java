package com.arkflame.smpmenus.menu;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class DefaultStaffMenuLayoutTest {
    private static FileConfiguration loadMenu() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/menus/staff.yml"));
    }

    @Test
    public void staffMenuRequiresStaffPermission() {
        Assertions.assertEquals("smpmenus.staff", loadMenu().getString("permission"));
    }

    @Test
    public void staffMenuUsesThreeRows() {
        Assertions.assertEquals(27, loadMenu().getInt("size"));
    }

    @Test
    public void fillerCoversWholeTopInventory() {
        Assertions.assertEquals(Collections.singletonList("0-26"), loadMenu().getStringList("items.filler_glass.slots"));
    }

    @Test
    public void fillerLosesPriorityToContentItems() {
        Assertions.assertEquals(-100, loadMenu().getInt("items.filler_glass.priority"));
    }

    @Test
    public void defaultStaffMenuClickCooldownIs500Milliseconds() {
        Assertions.assertEquals(500, loadMenu().getInt("click_cooldown"));
    }

    @Test
    public void staffActionItemsRequireStaffClickPermission() {
        final FileConfiguration menu = loadMenu();
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.god.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.fly.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.creative.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.survival.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.staff_mode.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.vanish.click_permission"));
        Assertions.assertEquals("smpmenus.staff", menu.getString("items.heal.click_permission"));
    }

    @Test
    public void staffItemsOccupyMiddleRow() {
        final FileConfiguration menu = loadMenu();
        Assertions.assertEquals(10, menu.getInt("items.god.slot"));
        Assertions.assertEquals(11, menu.getInt("items.fly.slot"));
        Assertions.assertEquals(12, menu.getInt("items.creative.slot"));
        Assertions.assertEquals(13, menu.getInt("items.survival.slot"));
        Assertions.assertEquals(14, menu.getInt("items.staff_mode.slot"));
        Assertions.assertEquals(15, menu.getInt("items.vanish.slot"));
        Assertions.assertEquals(16, menu.getInt("items.heal.slot"));
    }

    @Test
    public void allCurrentItemsRemainConfigured() {
        final ConfigurationSection items = loadMenu().getConfigurationSection("items");
        Assertions.assertEquals(
                new TreeSet<String>(Arrays.asList(
                        "creative",
                        "filler_glass",
                        "fly",
                        "god",
                        "heal",
                        "staff_mode",
                        "survival",
                        "vanish"
                )),
                new TreeSet<String>(items.getKeys(false))
        );
    }
}
