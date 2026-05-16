package com.arkflame.smpmenus.requirement;

import com.arkflame.smpmenus.util.MaterialResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class HasItemRequirement implements Requirement {
    private final String materialName;
    private final int amount;
    private final int data;

    public HasItemRequirement(final String materialName, final int amount, final int data) {
        this.materialName = materialName;
        this.amount = Math.max(1, amount);
        this.data = data;
    }

    @Override
    public boolean passes(final Player player, final String menuId) {
        if (player == null) {
            return false;
        }
        final MaterialResolver.ResolvedMaterial resolved = MaterialResolver.resolve(materialName, null, data, Material.AIR);
        int found = 0;
        for (final ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != resolved.getMaterial()) {
                continue;
            }
            if (data >= 0 && item.getDurability() != resolved.getData()) {
                continue;
            }
            found += item.getAmount();
            if (found >= amount) {
                return true;
            }
        }
        return false;
    }
}
