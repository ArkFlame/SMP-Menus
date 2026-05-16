package com.arkflame.smpmenus.util;

import com.arkflame.smpmenus.config.PluginSettings;
import com.arkflame.smpmenus.hook.PlaceholderHook;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageService {
    private final PluginSettings settings;
    private final PlaceholderHook placeholderHook;

    public MessageService(final PluginSettings settings, final PlaceholderHook placeholderHook) {
        this.settings = settings;
        this.placeholderHook = placeholderHook;
    }

    public void sendConfigMessage(final CommandSender sender, final String path, final String fallback) {
        send(sender, settings.getMessage(path, fallback), null);
    }

    public void send(final CommandSender sender, final String message, final String menuId) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        final Player player = sender instanceof Player ? (Player) sender : null;
        final String placeholders = placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, message, menuId));
        sender.sendMessage(ColorUtil.color(placeholders));
    }

    public void sendLines(final CommandSender sender, final List<String> messages, final String menuId) {
        if (messages == null) {
            return;
        }
        for (final String message : messages) {
            send(sender, message, menuId);
        }
    }

    public String render(final Player player, final String raw, final String menuId) {
        return ColorUtil.color(placeholderHook.apply(player, StringUtil.replaceBuiltIns(player, raw, menuId)));
    }
}
