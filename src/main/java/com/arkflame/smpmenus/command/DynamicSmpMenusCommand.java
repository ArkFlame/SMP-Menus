package com.arkflame.smpmenus.command;

import com.arkflame.smpmenus.SMPMenusPlugin;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public final class DynamicSmpMenusCommand extends Command implements PluginIdentifiableCommand {
    private final SMPMenusPlugin plugin;
    private final SmpMenusCommand executor;
    private final DynamicMenuTabCompleter tabCompleter;

    DynamicSmpMenusCommand(final SMPMenusPlugin plugin, final SmpMenusCommand executor,
            final DynamicMenuTabCompleter tabCompleter) {
        super("smpmenus");
        this.plugin = plugin;
        this.executor = executor;
        this.tabCompleter = tabCompleter;
        setDescription("SMPMenus admin command.");
        setUsage("/smpmenus [reload|open|list]");
        final java.util.List<String> aliases = new java.util.ArrayList<String>();
        aliases.add("smpmenusadmin");
        aliases.add("smpm");
        aliases.add("smphelp");
        aliases.add("smphelpadmin");
        aliases.add("smph");
        setAliases(aliases);
    }

    @Override
    public boolean execute(final CommandSender sender, final String label, final String[] args) {
        return executor.onCommand(sender, this, label, args);
    }

    @Override
    public java.util.List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
        final java.util.List<String> result = tabCompleter.completeAdmin(sender, args);
        return result != null ? result : java.util.Collections.<String>emptyList();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}