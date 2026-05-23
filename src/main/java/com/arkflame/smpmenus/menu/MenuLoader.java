package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementFactory;
import com.arkflame.smpmenus.requirement.RequirementGroup;
import com.arkflame.smpmenus.util.SlotParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MenuLoader {
    private final RequirementFactory requirementFactory;
    private final Logger logger;

    public MenuLoader(final RequirementFactory requirementFactory, final Logger logger) {
        this.requirementFactory = requirementFactory;
        this.logger = logger;
    }

    public ConfiguredMenu load(final File file) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        final String id = stripExtension(file.getName());
        final boolean enabled = config.getBoolean("enabled", true);
        final String permission = config.getString("permission", config.getString("open_permission", ""));
        final int size = resolveSize(config);
        final String title = config.getString("menu_title", config.getString("title", "&8Menu"));
        final List<String> openCommands = new ArrayList<String>(config.getStringList("open_command"));
        if (openCommands.isEmpty()) {
            final String single = config.getString("open_command");
            if (single != null && !single.trim().isEmpty()) {
                openCommands.add(single);
            }
        }
        final boolean registerCommand = config.getBoolean("register_command", true);
        final int updateInterval = Math.max(0, config.getInt("update_interval", 0));
        final boolean closeOnClick = config.getBoolean("close_on_click", false);
        final long clickCooldownMillis = Math.max(0L, config.getLong("click_cooldown", 0L));
        final List<String> openActions = config.getStringList("open_commands");
        final List<MenuItemDefinition> items = loadItems(config.getConfigurationSection("items"), size, closeOnClick);
        return new ConfiguredMenu(id, enabled, permission, title, size, openCommands, registerCommand, updateInterval, closeOnClick, clickCooldownMillis, openActions, items);
    }

    private List<MenuItemDefinition> loadItems(final ConfigurationSection section, final int size, final boolean menuCloseOnClick) {
        if (section == null) {
            return Collections.emptyList();
        }
        final List<MenuItemDefinition> items = new ArrayList<MenuItemDefinition>();
        for (final String key : section.getKeys(false)) {
            final ConfigurationSection item = section.getConfigurationSection(key);
            if (item == null) {
                continue;
            }
            final MenuItemDefinition definition = loadItem(key, item, size, menuCloseOnClick);
            if (!definition.getSlots().isEmpty()) {
                items.add(definition);
            }
        }
        Collections.sort(items, new Comparator<MenuItemDefinition>() {
            @Override
            public int compare(final MenuItemDefinition left, final MenuItemDefinition right) {
                return Integer.compare(left.getPriority(), right.getPriority());
            }
        });
        return items;
    }

    private MenuItemDefinition loadItem(final String id, final ConfigurationSection section, final int size, final boolean menuCloseOnClick) {
        final List<Integer> slots = SlotParser.parseSlots(section.get("slot"), section.getStringList("slots"), size);
        final RequirementGroup viewRequirement = requirementFactory.load(section.getConfigurationSection("view_requirement"));
        final Map<MenuClickType, RequirementGroup> clickRequirements = new EnumMap<MenuClickType, RequirementGroup>(MenuClickType.class);
        putRequirement(clickRequirements, MenuClickType.ANY, section.getConfigurationSection("click_requirement"));
        putRequirement(clickRequirements, MenuClickType.LEFT, section.getConfigurationSection("left_click_requirement"));
        putRequirement(clickRequirements, MenuClickType.RIGHT, section.getConfigurationSection("right_click_requirement"));
        putRequirement(clickRequirements, MenuClickType.SHIFT_LEFT, section.getConfigurationSection("shift_left_click_requirement"));
        putRequirement(clickRequirements, MenuClickType.SHIFT_RIGHT, section.getConfigurationSection("shift_right_click_requirement"));
        final Map<MenuClickType, List<String>> commands = new EnumMap<MenuClickType, List<String>>(MenuClickType.class);
        putCommands(commands, MenuClickType.ANY, section.getStringList("click_commands"));
        putCommands(commands, MenuClickType.LEFT, section.getStringList("left_click_commands"));
        putCommands(commands, MenuClickType.RIGHT, section.getStringList("right_click_commands"));
        putCommands(commands, MenuClickType.SHIFT_LEFT, section.getStringList("shift_left_click_commands"));
        putCommands(commands, MenuClickType.SHIFT_RIGHT, section.getStringList("shift_right_click_commands"));
        final String fallbackCommand = section.getString("cmd", "");
        if (commands.isEmpty() && fallbackCommand != null && !fallbackCommand.trim().isEmpty()) {
            final List<String> fallback = new ArrayList<String>();
            if (section.getBoolean("close_on_click", menuCloseOnClick)) {
                fallback.add("[close]");
            }
            fallback.add("[player] " + fallbackCommand);
            commands.put(MenuClickType.ANY, fallback);
        }
        final String clickPermission = section.getString("click_permission", section.getString("permission", ""));
        return new MenuItemDefinition(
                id,
                section.getString("material", section.getString("item", "CHEST")),
                section.getString("legacy_material", null),
                section.getInt("data", section.getInt("damage", 0)),
                section.getInt("damage", 0),
                section.getString("rgb", ""),
                section.getInt("amount", 1),
                section.getString("display_name", section.getString("name", "&f" + id)),
                section.getStringList("lore"),
                slots,
                section.getInt("priority", 0),
                section.getBoolean("glow", false),
                section.getBoolean("unbreakable", false),
                section.getBoolean("hide_attributes", true),
                section.getBoolean("close_on_click", menuCloseOnClick),
                clickPermission,
                viewRequirement,
                clickRequirements,
                commands
        );
    }

    private void putRequirement(final Map<MenuClickType, RequirementGroup> map, final MenuClickType type, final ConfigurationSection section) {
        if (section != null) {
            map.put(type, requirementFactory.load(section));
        }
    }

    private static void putCommands(final Map<MenuClickType, List<String>> map, final MenuClickType type, final List<String> commands) {
        if (commands != null && !commands.isEmpty()) {
            map.put(type, new ArrayList<String>(commands));
        }
    }

    private int resolveSize(final FileConfiguration config) {
        final int configuredSize = config.getInt("size", -1);
        if (configuredSize > 0) {
            return normalizeSize(configuredSize);
        }
        final int rows = config.getInt("rows", 3);
        return normalizeSize(rows * 9);
    }

    private int normalizeSize(final int requested) {
        final int clamped = Math.max(9, Math.min(54, requested));
        final int normalized = clamped - (clamped % 9);
        if (normalized != requested) {
            logger.warning("Menu size " + requested + " normalized to " + normalized + ". Inventory size must be 9..54 and multiple of 9.");
        }
        return normalized;
    }

    private static String stripExtension(final String fileName) {
        final int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
