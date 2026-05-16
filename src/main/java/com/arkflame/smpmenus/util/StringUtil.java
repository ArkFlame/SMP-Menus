package com.arkflame.smpmenus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class StringUtil {
    private StringUtil() {
    }

    public static String normalizeKey(final String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    public static String stripLeadingSlash(final String command) {
        if (command == null) {
            return "";
        }
        String result = command.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    public static String firstToken(final String commandLine) {
        final String command = stripLeadingSlash(commandLine);
        final int space = command.indexOf(' ');
        return space >= 0 ? command.substring(0, space) : command;
    }

    public static String[] commandArguments(final String commandLine) {
        final String command = stripLeadingSlash(commandLine);
        if (command.isEmpty()) {
            return new String[0];
        }
        final String[] parts = command.split("\\s+");
        if (parts.length <= 1) {
            return new String[0];
        }
        final String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    public static String replaceBuiltIns(final Player player, final String value, final String menuId) {
        if (value == null) {
            return "";
        }
        String result = value;
        if (player != null) {
            result = result.replace("%player_name%", player.getName());
            result = result.replace("%player%", player.getName());
            result = result.replace("%player_uuid%", player.getUniqueId().toString());
        }
        result = result.replace("%server_online%", Integer.toString(Bukkit.getOnlinePlayers().size()));
        result = result.replace("%menu%", menuId == null ? "" : menuId);
        return result;
    }

    public static List<String> copyStringList(final List<String> input) {
        final List<String> output = new ArrayList<String>();
        if (input == null) {
            return output;
        }
        output.addAll(input);
        return output;
    }
}
