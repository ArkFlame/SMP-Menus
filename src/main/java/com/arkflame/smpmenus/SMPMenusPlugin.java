package com.arkflame.smpmenus;

import com.arkflame.smpmenus.command.DynamicCommandRegistry;
import com.arkflame.smpmenus.command.SmpMenusCommand;
import com.arkflame.smpmenus.config.PluginSettings;
import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.listener.DynamicCommandRefreshListener;
import com.arkflame.smpmenus.listener.HelpCommandInterceptListener;
import com.arkflame.smpmenus.listener.MenuClickListener;
import com.arkflame.smpmenus.listener.MenuCloseListener;
import com.arkflame.smpmenus.listener.MenuDragListener;
import com.arkflame.smpmenus.listener.PlayerQuitListener;
import com.arkflame.smpmenus.menu.MenuManager;
import com.arkflame.smpmenus.util.FoliaAPI;
import com.arkflame.smpmenus.util.MessageService;
import com.arkflame.smpmenus.util.SoundService;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SMPMenusPlugin extends JavaPlugin {
    private PluginSettings settings;
    private PlaceholderHook placeholderHook;
    private MessageService messageService;
    private SoundService soundService;
    private MenuManager menuManager;
    private DynamicCommandRegistry commandRegistry;

    @Override
    public void onEnable() {
        FoliaAPI.init(this);
        saveDefaultConfig();
        saveDefaultMenusIfAbsent();
        reloadRuntimeState();
        registerCommands();
        registerListeners();
        getLogger().info("SMPMenus enabled. Menus loaded: " + menuManager.getMenuIds().size());
    }

    @Override
    public void onDisable() {
        if (menuManager != null) {
            if (settings != null && settings.isCloseOpenMenuOnDisable()) {
                menuManager.closeAllOpenMenus();
            }
            menuManager.close();
        }
        if (commandRegistry != null) {
            commandRegistry.shutdown();
        }
        FoliaAPI.cancelAllTasks();
        HandlerList.unregisterAll(this);
    }

    public void reloadRuntimeState() {
        reloadConfig();
        this.settings = PluginSettings.from(getConfig());
        this.placeholderHook = new PlaceholderHook(this);
        this.messageService = new MessageService(settings, placeholderHook);
        this.soundService = new SoundService(settings);
        if (this.menuManager != null) {
            this.menuManager.close();
        }
        this.menuManager = new MenuManager(this, messageService, soundService, placeholderHook, settings);
        this.menuManager.reload();
        if (this.commandRegistry != null) {
            this.commandRegistry.reload();
        }
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public SoundService getSoundService() {
        return soundService;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public DynamicCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    private void registerCommands() {
        this.commandRegistry = new DynamicCommandRegistry(this, new SmpMenusCommand(this));
        this.commandRegistry.reload();
    }

    private void registerListeners() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new MenuClickListener(this), this);
        pluginManager.registerEvents(new MenuDragListener(this), this);
        pluginManager.registerEvents(new MenuCloseListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new HelpCommandInterceptListener(this), this);
        pluginManager.registerEvents(new DynamicCommandRefreshListener(this), this);
    }

    private void saveDefaultMenusIfAbsent() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder.");
            return;
        }
        final java.io.File menusFolder = new java.io.File(getDataFolder(), "menus");
        if (menusFolder.exists()) {
            return;
        }
        if (!menusFolder.mkdirs()) {
            getLogger().warning("Could not create menus folder: " + menusFolder.getPath());
            return;
        }
        saveResource("menus/help.yml", false);
        saveResource("menus/rules.yml", false);
        saveResource("menus/staff.yml", false);
    }
}
