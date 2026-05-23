package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.requirement.RequirementGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public final class MenuItemDefinition {
    private final String id;
    private final String material;
    private final String legacyMaterial;
    private final int data;
    private final int damage;
    private final String rgb;
    private final int amount;
    private final String displayName;
    private final List<String> lore;
    private final List<Integer> slots;
    private final int priority;
    private final boolean glow;
    private final boolean unbreakable;
    private final boolean hideAttributes;
    private final boolean closeOnClick;
    private final String clickPermission;
    private final RequirementGroup viewRequirement;
    private final Map<MenuClickType, RequirementGroup> clickRequirements;
    private final Map<MenuClickType, List<String>> clickCommands;

    public MenuItemDefinition(
            final String id,
            final String material,
            final String legacyMaterial,
            final int data,
            final int damage,
            final String rgb,
            final int amount,
            final String displayName,
            final List<String> lore,
            final List<Integer> slots,
            final int priority,
            final boolean glow,
            final boolean unbreakable,
            final boolean hideAttributes,
            final boolean closeOnClick,
            final String clickPermission,
            final RequirementGroup viewRequirement,
            final Map<MenuClickType, RequirementGroup> clickRequirements,
            final Map<MenuClickType, List<String>> clickCommands
    ) {
        this.id = id;
        this.material = material;
        this.legacyMaterial = legacyMaterial;
        this.data = data;
        this.damage = damage;
        this.rgb = normalizeRgb(rgb);
        this.amount = amount;
        this.displayName = displayName;
        this.lore = Collections.unmodifiableList(new ArrayList<String>(lore));
        this.slots = Collections.unmodifiableList(new ArrayList<Integer>(slots));
        this.priority = priority;
        this.glow = glow;
        this.unbreakable = unbreakable;
        this.hideAttributes = hideAttributes;
        this.closeOnClick = closeOnClick;
        this.clickPermission = normalizePermission(clickPermission);
        this.viewRequirement = viewRequirement;
        this.clickRequirements = new EnumMap<MenuClickType, RequirementGroup>(clickRequirements);
        this.clickCommands = copyCommands(clickCommands);
    }

    private static String normalizePermission(final String permission) {
        return permission == null ? "" : permission.trim();
    }

    private static String normalizeRgb(final String rgb) {
        return rgb == null ? "" : rgb.trim();
    }

    private static Map<MenuClickType, List<String>> copyCommands(final Map<MenuClickType, List<String>> source) {
        final Map<MenuClickType, List<String>> output = new EnumMap<MenuClickType, List<String>>(MenuClickType.class);
        for (final Map.Entry<MenuClickType, List<String>> entry : source.entrySet()) {
            output.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<String>(entry.getValue())));
        }
        return output;
    }

    public RequirementGroup getClickRequirement(final MenuClickType clickType) {
        final RequirementGroup specific = clickRequirements.get(clickType);
        if (specific != null) {
            return specific;
        }
        final RequirementGroup any = clickRequirements.get(MenuClickType.ANY);
        return any == null ? RequirementGroup.empty() : any;
    }

    public List<String> getClickCommands(final MenuClickType clickType) {
        final List<String> specific = clickCommands.get(clickType);
        if (specific != null && !specific.isEmpty()) {
            return specific;
        }
        final List<String> any = clickCommands.get(MenuClickType.ANY);
        return any == null ? Collections.<String>emptyList() : any;
    }

    public boolean hasClickPermission() {
        return !clickPermission.isEmpty();
    }

    public boolean hasRgb() {
        return !rgb.isEmpty();
    }
}
