package com.arkflame.smpmenus.requirement;

import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.util.StringUtil;
import org.bukkit.entity.Player;

public final class NumericRequirement implements Requirement {
    private final PlaceholderHook placeholderHook;
    private final String operator;
    private final String input;
    private final String output;

    public NumericRequirement(final PlaceholderHook placeholderHook, final String operator, final String input, final String output) {
        this.placeholderHook = placeholderHook;
        this.operator = operator;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean passes(final Player player, final String menuId) {
        final Double left = parse(placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, input, menuId)));
        final Double right = parse(placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, output, menuId)));
        if (left == null || right == null) {
            return false;
        }
        if (">=".equals(operator)) {
            return left >= right;
        }
        if (">".equals(operator)) {
            return left > right;
        }
        if ("<=".equals(operator)) {
            return left <= right;
        }
        if ("<".equals(operator)) {
            return left < right;
        }
        if ("!=".equals(operator)) {
            return Double.compare(left, right) != 0;
        }
        return Double.compare(left, right) == 0;
    }

    private static Double parse(final String value) {
        try {
            return Double.valueOf(value.replace(",", "").trim());
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
