package com.arkflame.smpmenus.menu;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class DefaultRulesMenuLayoutTest {
    private static FileConfiguration loadMenu() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/menus/rules.yml"));
    }

    @Test
    public void defaultRulesMenuClickCooldownIs500Milliseconds() {
        Assertions.assertEquals(500, loadMenu().getInt("click_cooldown"));
    }
}