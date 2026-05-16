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

public final class DefaultHelpMenuLayoutTest {
    private static FileConfiguration loadMenu() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/menus/help.yml"));
    }

    @Test
    public void defaultMenuUsesFiveRows() {
        Assertions.assertEquals(45, loadMenu().getInt("size"));
    }

    @Test
    public void fillerCoversWholeTopInventory() {
        Assertions.assertEquals(Collections.singletonList("0-44"), loadMenu().getStringList("items.filler_glass.slots"));
    }

    @Test
    public void fillerUsesGrayGlass() {
        Assertions.assertEquals("GRAY_STAINED_GLASS_PANE", loadMenu().getString("items.filler_glass.material"));
    }

    @Test
    public void fillerUsesLegacyGrayData() {
        Assertions.assertEquals(7, loadMenu().getInt("items.filler_glass.data"));
    }

    @Test
    public void fillerLosesPriorityToContentItems() {
        Assertions.assertEquals(-100, loadMenu().getInt("items.filler_glass.priority"));
    }

    @Test
    public void closeButtonIsCenteredOnLowestRow() {
        Assertions.assertEquals(40, loadMenu().getInt("items.close.slot"));
    }

    @Test
    public void rulesMovedToLowerSection() {
        Assertions.assertEquals(29, loadMenu().getInt("items.rules.slot"));
    }

    @Test
    public void rulesItemUsesInternalMenuAction() {
        Assertions.assertEquals(Collections.singletonList("[menu] rules"), loadMenu().getStringList("items.rules.click_commands"));
    }

    @Test
    public void tpaItemIsAlignedOnLowerRow() {
        Assertions.assertEquals(28, loadMenu().getInt("items.tpa.slot"));
    }

    @Test
    public void homesItemIsAlignedOnLowerRow() {
        Assertions.assertEquals(30, loadMenu().getInt("items.homes.slot"));
    }

    @Test
    public void teamMovedToLowerCenter() {
        Assertions.assertEquals(31, loadMenu().getInt("items.team.slot"));
    }

    @Test
    public void mediaMovedThreeSlotsRight() {
        Assertions.assertEquals(33, loadMenu().getInt("items.media.slot"));
    }

    @Test
    public void cratesItemIsAlignedOnLowerRow() {
        Assertions.assertEquals(32, loadMenu().getInt("items.crates.slot"));
    }

    @Test
    public void lowerHelpRowIsContinuousFromTpaToMedia() {
        Assertions.assertEquals(28, loadMenu().getInt("items.tpa.slot"));
        Assertions.assertEquals(29, loadMenu().getInt("items.rules.slot"));
        Assertions.assertEquals(30, loadMenu().getInt("items.homes.slot"));
        Assertions.assertEquals(31, loadMenu().getInt("items.team.slot"));
        Assertions.assertEquals(32, loadMenu().getInt("items.crates.slot"));
        Assertions.assertEquals(33, loadMenu().getInt("items.media.slot"));
    }

    @Test
    public void coinflipIsCenteredInMiddleSlot() {
        Assertions.assertEquals(22, loadMenu().getInt("items.coinflip.slot"));
    }

    @Test
    public void discordUsesBaseHead() {
        Assertions.assertTrue(loadMenu().getString("items.discord.material", "").startsWith("basehead-"));
    }

    @Test
    public void coinflipUsesBaseHead() {
        Assertions.assertTrue(loadMenu().getString("items.coinflip.material", "").startsWith("basehead-"));
    }

    @Test
    public void defaultHelpMenuClickCooldownIs500Milliseconds() {
        Assertions.assertEquals(500, loadMenu().getInt("click_cooldown"));
    }

    @Test
    public void allCurrentItemsRemainConfigured() {
        final ConfigurationSection items = loadMenu().getConfigurationSection("items");
        Assertions.assertEquals(
                new TreeSet<String>(Arrays.asList(
                        "afk",
                        "auction",
                        "close",
                        "coinflip",
                        "crates",
                        "discord",
                        "filler_glass",
                        "homes",
                        "kits",
                        "leaderboards",
                        "lifesteal",
                        "media",
                        "orders",
                        "rules",
                        "rtp",
                        "sell",
                        "shop",
                        "stats",
                        "store",
                        "team",
                        "tpa",
                        "world_info"
                )),
                new TreeSet<String>(items.getKeys(false))
        );
    }
}
