package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public final class MenuLoaderPermissionTest {
    @TempDir
    private File tempDir;

    @Test
    public void loadsTopLevelMenuPermission() throws IOException {
        final File file = writeMenu("staff.yml", "permission: smpmenus.staff\nitems: {}\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("smpmenus.staff", menu.getPermission());
    }

    @Test
    public void missingTopLevelMenuPermissionDefaultsBlank() throws IOException {
        final File file = writeMenu("help.yml", "items: {}\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("", menu.getPermission());
    }

    @Test
    public void loadsItemClickPermission() throws IOException {
        final File file = writeMenu("staff.yml",
                "items:\n" +
                "  god:\n" +
                "    material: GOLDEN_APPLE\n" +
                "    slot: 0\n" +
                "    click_permission: smpmenus.staff\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("smpmenus.staff", menu.getItems().get(0).getClickPermission());
    }

    @Test
    public void itemPermissionAliasLoadsAsClickPermission() throws IOException {
        final File file = writeMenu("staff.yml",
                "items:\n" +
                "  god:\n" +
                "    material: GOLDEN_APPLE\n" +
                "    slot: 0\n" +
                "    permission: smpmenus.staff\n");
        final ConfiguredMenu menu = newLoader().load(file);
        Assertions.assertEquals("smpmenus.staff", menu.getItems().get(0).getClickPermission());
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
        final Logger logger = Logger.getLogger(MenuLoaderPermissionTest.class.getName());
        return new MenuLoader(new RequirementFactory(null, logger), logger);
    }
}
