package com.arkflame.smpmenus.menu;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class PluginDescriptorTest {
    private static FileConfiguration loadDescriptor() {
        return YamlConfiguration.loadConfiguration(new File("src/main/resources/plugin.yml"));
    }

    @Test
    public void softDependsOnPlaceholderApi() {
        Assertions.assertTrue(loadDescriptor().getStringList("softdepend").contains("PlaceholderAPI"));
    }

    @Test
    public void staffPermissionDefaultsToOp() {
        Assertions.assertEquals("op", loadDescriptor().getString("permissions.smpmenus.staff.default"));
    }
}
