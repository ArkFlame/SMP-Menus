package com.arkflame.smpmenus.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public final class PluginSettings {
    private final String mainMenu;
    private final boolean interceptHelpCommand;
    private final boolean interceptMenuOpenCommands;
    private final boolean reloadReopensActiveMenus;
    private final boolean closeOpenMenuOnDisable;
    private final boolean debug;
    private final FileConfiguration config;

    private PluginSettings(
            final String mainMenu,
            final boolean interceptHelpCommand,
            final boolean interceptMenuOpenCommands,
            final boolean reloadReopensActiveMenus,
            final boolean closeOpenMenuOnDisable,
            final boolean debug,
            final FileConfiguration config
    ) {
        this.mainMenu = mainMenu;
        this.interceptHelpCommand = interceptHelpCommand;
        this.interceptMenuOpenCommands = interceptMenuOpenCommands;
        this.reloadReopensActiveMenus = reloadReopensActiveMenus;
        this.closeOpenMenuOnDisable = closeOpenMenuOnDisable;
        this.debug = debug;
        this.config = config;
    }

    public static PluginSettings from(final FileConfiguration config) {
        return new PluginSettings(
                config.getString("main-menu", "help"),
                config.getBoolean("intercept-help-command", true),
                config.getBoolean("intercept-menu-open-commands", true),
                config.getBoolean("reload-reopens-active-menus", true),
                config.getBoolean("close-open-menu-on-disable", true),
                config.getBoolean("debug", false),
                config
        );
    }

    public String getMessage(final String path, final String fallback) {
        return config.getString("messages." + path, fallback);
    }

    public SoundSetting getSound(final String path) {
        final ConfigurationSection section = config.getConfigurationSection("sounds." + path);
        if (section == null) {
            return SoundSetting.disabled();
        }
        return new SoundSetting(
                section.getBoolean("enabled", section.getBoolean("enable", true)),
                section.getString("sound", "UI_BUTTON_CLICK"),
                (float) section.getDouble("volume", 1.0D),
                (float) section.getDouble("pitch", 1.0D)
        );
    }

    @Getter
    public static final class SoundSetting {
        private final boolean enabled;
        private final String sound;
        private final float volume;
        private final float pitch;

        public SoundSetting(final boolean enabled, final String sound, final float volume, final float pitch) {
            this.enabled = enabled;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        public static SoundSetting disabled() {
            return new SoundSetting(false, "", 1.0F, 1.0F);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getSound() {
            return sound;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
