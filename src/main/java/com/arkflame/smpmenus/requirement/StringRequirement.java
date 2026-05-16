package com.arkflame.smpmenus.requirement;

import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.util.StringUtil;
import org.bukkit.entity.Player;

public final class StringRequirement implements Requirement {
    private final PlaceholderHook placeholderHook;
    private final String input;
    private final String output;
    private final boolean ignoreCase;

    public StringRequirement(final PlaceholderHook placeholderHook, final String input, final String output, final boolean ignoreCase) {
        this.placeholderHook = placeholderHook;
        this.input = input;
        this.output = output;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean passes(final Player player, final String menuId) {
        final String renderedInput = placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, input, menuId));
        final String renderedOutput = placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, output, menuId));
        return ignoreCase ? renderedInput.equalsIgnoreCase(renderedOutput) : renderedInput.equals(renderedOutput);
    }
}
