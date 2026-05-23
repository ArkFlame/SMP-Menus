package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class MenuLoaderRgbTest {
    @TempDir
    private File tempDir;

    @Test
    public void loadsRgbOption() throws IOException {
        final File file = writeMenu("rgb.yml",
                "items:\n" +
                "  armor:\n" +
                "    material: LEATHER_CHESTPLATE\n" +
                "    slot: 0\n" +
                "    rgb: '38, 192, 210'\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("38, 192, 210", menu.getItems().get(0).getRgb());
    }

    @Test
    public void missingRgbDefaultsBlank() throws IOException {
        final File file = writeMenu("missing.yml",
                "items:\n" +
                "  armor:\n" +
                "    material: LEATHER_CHESTPLATE\n" +
                "    slot: 0\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("", menu.getItems().get(0).getRgb());
    }

    private File writeMenu(final String name, final String extraYaml) throws IOException {
        final File file = new File(tempDir, name);
        final FileWriter writer = new FileWriter(file);
        try {
            writer.write("enabled: true\n");
            writer.write("menu_title: '&8Test'\n");
            writer.write("size: 9\n");
            writer.write(extraYaml);
        } finally {
            writer.close();
        }
        return file;
    }

    private static MenuLoader newLoader() {
        final Logger logger = Logger.getLogger(MenuLoaderRgbTest.class.getName());
        return new MenuLoader(new RequirementFactory(null, logger), logger);
    }
}
