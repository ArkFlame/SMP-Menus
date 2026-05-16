package com.arkflame.smpmenus.util;

import com.arkflame.smpmenus.config.PluginSettings;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundService {
    private final PluginSettings settings;

    public SoundService(final PluginSettings settings) {
        this.settings = settings;
    }

    public void playConfigured(final Player player, final String path) {
        final PluginSettings.SoundSetting setting = settings.getSound(path);
        if (!setting.isEnabled()) {
            return;
        }
        play(player, setting.getSound(), setting.getVolume(), setting.getPitch());
    }

    public void play(final Player player, final String soundName, final float volume, final float pitch) {
        if (player == null || soundName == null || soundName.trim().isEmpty()) {
            return;
        }
        final java.util.Optional<Sound> sound = SoundResolver.resolve(soundName);
        if (!sound.isPresent()) {
            return;
        }
        player.playSound(player.getLocation(), sound.get(), volume, pitch);
    }
}
