package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class MenuLoaderClickCooldownTest {
    @TempDir
    private File tempDir;

    @Test
    public void loadsClickCooldownMilliseconds() throws IOException {
        final File file = writeMenu("cooldown.yml", "click_cooldown: 500\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals(500L, menu.getClickCooldownMillis());
    }

    @Test
    public void missingClickCooldownDefaultsToZero() throws IOException {
        final File file = writeMenu("missing.yml", "");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals(0L, menu.getClickCooldownMillis());
    }

    @Test
    public void negativeClickCooldownDefaultsToZero() throws IOException {
        final File file = writeMenu("negative.yml", "click_cooldown: -25\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals(0L, menu.getClickCooldownMillis());
    }

    private File writeMenu(final String name, final String extraYaml) throws IOException {
        final File file = new File(tempDir, name);
        final FileWriter writer = new FileWriter(file);
        try {
            writer.write("enabled: true\n");
            writer.write("menu_title: '&8Test'\n");
            writer.write("size: 9\n");
            writer.write(extraYaml);
            writer.write("items: {}\n");
        } finally {
            writer.close();
        }
        return file;
    }

    private static MenuLoader newLoader() {
        final Logger logger = Logger.getLogger(MenuLoaderClickCooldownTest.class.getName());
        return new MenuLoader(new RequirementFactory(null, logger), logger);
    }
}