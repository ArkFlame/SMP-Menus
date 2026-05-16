package com.arkflame.smpmenus.gui;

import com.arkflame.smpmenus.head.HeadItemFactory;
import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.menu.MenuItemDefinition;
import com.arkflame.smpmenus.menu.MenuOpenContext;
import com.arkflame.smpmenus.util.MaterialResolver;
import com.arkflame.smpmenus.util.MessageService;
import com.arkflame.smpmenus.util.ReflectionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public final class ItemBuilder {
    private final Plugin plugin;
    private final MessageService messageService;
    private final HeadItemFactory headItemFactory;

    public ItemBuilder(final Plugin plugin, final MessageService messageService, final PlaceholderHook placeholderHook) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.headItemFactory = new HeadItemFactory(plugin, placeholderHook);
    }

    public ItemStack build(final Player player, final String menuId, final MenuOpenContext context, final MenuItemDefinition definition) {
        final Optional<ItemStack> head = headItemFactory.create(player, menuId, context, definition);
        final ItemStack item;
        final int amount;
        if (head.isPresent()) {
            item = head.get();
            amount = Math.max(1, Math.min(64, definition.getAmount()));
        } else {
            final MaterialResolver.ResolvedMaterial resolved = MaterialResolver.resolve(
                    definition.getMaterial(), definition.getLegacyMaterial(), definition.getData(), Material.CHEST
            );
            amount = Math.max(1, Math.min(64, definition.getAmount()));
            item = new ItemStack(resolved.getMaterial(), amount, resolved.getData());
            item.setDurability((short) Math.max(resolved.getData(), definition.getDamage()));
        }
        item.setAmount(amount);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            applyMeta(player, menuId, definition, meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void applyMeta(final Player player, final String menuId, final MenuItemDefinition definition, final ItemMeta meta) {
        meta.setDisplayName(messageService.render(player, definition.getDisplayName(), menuId));
        final List<String> lore = new ArrayList<String>();
        for (final String line : definition.getLore()) {
            lore.add(messageService.render(player, line, menuId));
        }
        meta.setLore(lore);
        if (definition.isUnbreakable()) {
            ReflectionUtil.invokeVoidSafe(meta, "setUnbreakable", new Object[] { Boolean.TRUE }, boolean.class);
        }
        if (definition.isHideAttributes()) {
            try {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            } catch (final NoClassDefFoundError ignored) {
            }
        }
        if (definition.isGlow()) {
            try {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } catch (final RuntimeException exception) {
                plugin.getLogger().log(Level.FINE, "Could not apply glow to menu item.", exception);
            }
        }
    }

    public void clearHeadCaches() {
        headItemFactory.clearCaches();
    }

    public void clearNamedHeadCache(final String playerName) {
        headItemFactory.clearNamedCache(playerName);
    }
}