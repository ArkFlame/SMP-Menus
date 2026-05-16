package com.arkflame.smpmenus.menu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.entity.Player;

public final class MenuOpenContext {
    private final String menuId;
    private final Map<String, String> variables;

    private MenuOpenContext(final String menuId, final Map<String, String> variables) {
        this.menuId = menuId == null ? "" : menuId;
        this.variables = Collections.unmodifiableMap(new HashMap<String, String>(variables));
    }

    public static MenuOpenContext of(final Player player, final String menuId) {
        return of(player, menuId, new String[0]);
    }

    public static MenuOpenContext of(final Player player, final String menuId, final String[] args) {
        final Map<String, String> map = new HashMap<String, String>();
        final String playerName = player == null ? "" : player.getName();
        map.put("player", playerName);
        map.put("player_name", playerName);
        map.put("viewer", playerName);
        map.put("uuid", player == null ? "" : player.getUniqueId().toString());
        map.put("menu", menuId == null ? "" : menuId);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                map.put("arg_" + (i + 1), args[i] == null ? "" : args[i]);
            }
        }
        map.put("target", args != null && args.length > 0 && args[0] != null && !args[0].isEmpty() ? args[0] : playerName);
        return new MenuOpenContext(menuId, map);
    }

    public String getMenuId() {
        return menuId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String replaceVariables(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String output = input;
        for (final Map.Entry<String, String> entry : variables.entrySet()) {
            output = output.replace("{" + entry.getKey().toLowerCase(Locale.ROOT) + "}", entry.getValue());
            output = output.replace("{" + entry.getKey().toUpperCase(Locale.ROOT) + "}", entry.getValue());
        }
        return output;
    }
}