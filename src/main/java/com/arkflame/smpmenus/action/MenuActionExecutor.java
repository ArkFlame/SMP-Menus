package com.arkflame.smpmenus.action;

import com.arkflame.smpmenus.menu.MenuManager;
import com.arkflame.smpmenus.util.FoliaAPI;
import com.arkflame.smpmenus.util.MessageService;
import com.arkflame.smpmenus.util.SoundService;
import com.arkflame.smpmenus.util.StringUtil;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class MenuActionExecutor {
    private final Plugin plugin;
    private final MessageService messageService;
    private final SoundService soundService;
    private final MenuManager menuManager;

    public MenuActionExecutor(
            final Plugin plugin,
            final MessageService messageService,
            final SoundService soundService,
            final MenuManager menuManager
    ) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.soundService = soundService;
        this.menuManager = menuManager;
    }

    public void executeAll(final Player player, final String menuId, final List<String> actions) {
        if (player == null || actions == null) {
            return;
        }
        for (final String action : actions) {
            execute(player, menuId, action);
        }
    }

    public void execute(final Player player, final String menuId, final String rawAction) {
        if (player == null || rawAction == null || rawAction.trim().isEmpty()) {
            return;
        }
        final ParsedAction parsed = ParsedAction.parse(rawAction);
        final String value = render(player, menuId, parsed.getValue());
        final String type = parsed.getType();
        if ("message".equals(type)) {
            messageService.send(player, value, menuId);
            return;
        }
        if ("broadcast".equals(type)) {
            for (final Player target : Bukkit.getOnlinePlayers()) {
                messageService.send(target, value, menuId);
            }
            return;
        }
        if ("sound".equals(type)) {
            soundService.play(player, value, 1.0F, 1.0F);
            return;
        }
        if ("close".equals(type)) {
            FoliaAPI.runTaskForEntity(player, new Runnable() {
                @Override
                public void run() {
                    player.closeInventory();
                }
            });
            return;
        }
        if ("refresh".equals(type)) {
            menuManager.refresh(player);
            return;
        }
        if ("menu".equals(type) || "open".equals(type)) {
            menuManager.open(player, value);
            return;
        }
        if ("console".equals(type)) {
            dispatchConsole(value);
            return;
        }
        if ("op".equals(type)) {
            dispatchOp(player, value);
            return;
        }
        if ("none".equals(type)) {
            return;
        }
        dispatchPlayer(player, value);
    }

    private String render(final Player player, final String menuId, final String value) {
        return plugin instanceof com.arkflame.smpmenus.SMPMenusPlugin
                ? ((com.arkflame.smpmenus.SMPMenusPlugin) plugin).getPlaceholderHook().apply(player, StringUtil.replaceBuiltIns(player, value, menuId))
                : StringUtil.replaceBuiltIns(player, value, menuId);
    }

    private void dispatchPlayer(final Player player, final String command) {
        final String cleaned = StringUtil.stripLeadingSlash(command);
        if (menuManager.openByCommandAlias(player, cleaned)) {
            return;
        }
        FoliaAPI.runTaskForEntity(player, new Runnable() {
            @Override
            public void run() {
                player.performCommand(cleaned);
            }
        });
    }

    private void dispatchConsole(final String command) {
        final String cleaned = StringUtil.stripLeadingSlash(command);
        FoliaAPI.runTask(new Runnable() {
            @Override
            public void run() {
                final ConsoleCommandSender console = Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(console, cleaned);
            }
        });
    }

    private void dispatchOp(final Player player, final String command) {
        final String cleaned = StringUtil.stripLeadingSlash(command);
        FoliaAPI.runTaskForEntity(player, new Runnable() {
            @Override
            public void run() {
                final boolean previous = player.isOp();
                try {
                    player.setOp(true);
                    player.performCommand(cleaned);
                } catch (final RuntimeException exception) {
                    plugin.getLogger().log(Level.WARNING, "OP menu command failed for " + player.getName() + ": " + cleaned, exception);
                } finally {
                    player.setOp(previous);
                }
            }
        });
    }

    private static final class ParsedAction {
        private final String type;
        private final String value;

        private ParsedAction(final String type, final String value) {
            this.type = type;
            this.value = value;
        }

        private String getType() {
            return type;
        }

        private String getValue() {
            return value;
        }

        private static ParsedAction parse(final String rawAction) {
            final String trimmed = rawAction.trim();
            if (trimmed.startsWith("[") && trimmed.contains("]")) {
                final int close = trimmed.indexOf(']');
                final String type = trimmed.substring(1, close).trim().toLowerCase(Locale.ROOT);
                final String value = trimmed.substring(close + 1).trim();
                return new ParsedAction(type, value);
            }
            return new ParsedAction("player", trimmed);
        }
    }
}
