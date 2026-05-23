package com.arkflame.smpmenus.gui;

import com.arkflame.smpmenus.util.ReflectionUtil;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;

public final class ItemColorApplier {
    private final Plugin plugin;
    private boolean potionColorUnsupportedLogged;

    public ItemColorApplier(final Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public boolean apply(final ItemMeta meta, final RgbColor rgbColor) {
        if (meta == null || rgbColor == null) {
            return false;
        }
        final Color color = rgbColor.toBukkitColor();
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
            return true;
        }
        if (meta instanceof FireworkEffectMeta) {
            final FireworkEffect effect = FireworkEffect.builder().withColor(color).build();
            ((FireworkEffectMeta) meta).setEffect(effect);
            return true;
        }
        if (meta instanceof PotionMeta) {
            final boolean applied = ReflectionUtil.invokeVoidSafe(meta, "setColor", new Object[] { color }, Color.class);
            if (!applied) {
                logPotionColorUnsupportedOnce();
            }
            return applied;
        }
        return false;
    }

    private void logPotionColorUnsupportedOnce() {
        if (potionColorUnsupportedLogged) {
            return;
        }
        potionColorUnsupportedLogged = true;
        plugin.getLogger().log(Level.FINE, "Potion RGB item color is unsupported by this Bukkit API; item will render without custom potion color.");
    }
}
