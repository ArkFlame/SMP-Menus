package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.action.MenuActionExecutor;
import com.arkflame.smpmenus.config.PluginSettings;
import com.arkflame.smpmenus.gui.ItemBuilder;
import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.requirement.RequirementFactory;
import com.arkflame.smpmenus.requirement.RequirementGroup;
import com.arkflame.smpmenus.util.FoliaAPI;
import com.arkflame.smpmenus.util.MessageService;
import com.arkflame.smpmenus.util.SoundService;
import com.arkflame.smpmenus.util.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class MenuManager {
    private final Plugin plugin;
    private final MessageService messageService;
    private final SoundService soundService;
    private final PlaceholderHook placeholderHook;
    private final PluginSettings settings;
    private final ItemBuilder itemBuilder;
    private final MenuActionExecutor actionExecutor;
    private final Map<String, ConfiguredMenu> menus = new LinkedHashMap<String, ConfiguredMenu>();
    private final Map<String, String> commandAliases = new HashMap<String, String>();
    private final Map<UUID, OpenMenuSession> sessions = new HashMap<UUID, OpenMenuSession>();
    private final ClickCooldownTracker clickCooldowns = new ClickCooldownTracker();

    public MenuManager(
            final Plugin plugin,
            final MessageService messageService,
            final SoundService soundService,
            final PlaceholderHook placeholderHook,
            final PluginSettings settings
    ) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.soundService = soundService;
        this.placeholderHook = placeholderHook;
        this.settings = settings;
        this.itemBuilder = new ItemBuilder(plugin, messageService, placeholderHook);
        this.actionExecutor = new MenuActionExecutor(plugin, messageService, soundService, this);
    }

    public void reload() {
        menus.clear();
        commandAliases.clear();
        final File folder = new File(plugin.getDataFolder(), "menus");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create menus folder: " + folder.getPath());
            return;
        }
        final RequirementFactory requirementFactory = new RequirementFactory(placeholderHook, plugin.getLogger());
        final MenuLoader loader = new MenuLoader(requirementFactory, plugin.getLogger());
        final File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (final File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".yml")) {
                continue;
            }
            final ConfiguredMenu menu = loader.load(file);
            menus.put(StringUtil.normalizeKey(menu.getId()), menu);
            if (menu.isEnabled() && menu.isRegisterCommand()) {
                for (final String command : menu.getOpenCommands()) {
                    commandAliases.put(StringUtil.normalizeKey(StringUtil.firstToken(command)), menu.getId());
                }
            }
        }
    }

    public void close() {
        for (final OpenMenuSession session : new ArrayList<OpenMenuSession>(sessions.values())) {
            session.cancelUpdateTask();
        }
        sessions.clear();
        clickCooldowns.clearAll();
        itemBuilder.clearHeadCaches();
    }

    public Set<String> getMenuIds() {
        return Collections.unmodifiableSet(new HashSet<String>(menus.keySet()));
    }

    public Set<String> getCommandAliases() {
        return Collections.unmodifiableSet(new HashSet<String>(commandAliases.keySet()));
    }

    public ConfiguredMenu getMenu(final String id) {
        if (id == null) {
            return null;
        }
        return menus.get(StringUtil.normalizeKey(id));
    }

    public String findMenuByCommand(final String command) {
        if (command == null) {
            return null;
        }
        return commandAliases.get(StringUtil.normalizeKey(StringUtil.firstToken(command)));
    }

    public boolean openByCommandAlias(final Player player, final String commandLine) {
        if (player == null || commandLine == null || commandLine.trim().isEmpty()) {
            return false;
        }
        final String menuId = findMenuByCommand(commandLine);
        if (menuId == null) {
            return false;
        }
        return open(player, menuId, MenuOpenContext.of(player, menuId, StringUtil.commandArguments(commandLine)));
    }

    public boolean openMain(final Player player) {
        return open(player, settings.getMainMenu());
    }

    public boolean open(final Player player, final String menuId) {
        return open(player, menuId, MenuOpenContext.of(player, menuId));
    }

    public boolean open(final Player player, final String menuId, final MenuOpenContext context) {
        if (player == null) {
            return false;
        }
        final ConfiguredMenu menu = getMenu(menuId);
        if (menu == null || !menu.isEnabled()) {
            messageService.send(player, settings.getMessage("unknown-menu", "&cUnknown menu: &f%menu%").replace("%menu%", String.valueOf(menuId)), menuId);
            return false;
        }
        if (!canOpen(player, menu)) {
            messageService.send(player, settings.getMessage("no-permission", "&cYou do not have permission to do that."), menu.getId());
            soundService.playConfigured(player, "deny");
            return false;
        }
        final MenuOpenContext safeContext = context == null ? MenuOpenContext.of(player, menuId) : context;
        FoliaAPI.runTaskForEntity(player, new Runnable() {
            @Override
            public void run() {
                openNow(player, menu, safeContext);
            }
        });
        return true;
    }

    public void refresh(final Player player) {
        if (player == null) {
            return;
        }
        final OpenMenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }
        final ConfiguredMenu menu = getMenu(session.getMenuId());
        if (menu == null) {
            return;
        }
        FoliaAPI.runTaskForEntity(player, new Runnable() {
            @Override
            public void run() {
                renderIntoCurrentInventory(player, menu, session.getContext());
            }
        });
    }

    public void onClose(final Player player) {
        if (player == null) {
            return;
        }
        final OpenMenuSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.cancelUpdateTask();
        }
    }

    public void onQuit(final Player player) {
        onClose(player);
        if (player != null) {
            clickCooldowns.clear(player.getUniqueId());
            itemBuilder.clearNamedHeadCache(player.getName());
        }
    }

    public boolean isMenuInventory(final Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        final InventoryHolder holder = inventory.getHolder();
        return holder instanceof MenuHolder;
    }

    public String getMenuId(final Inventory inventory) {
        if (!isMenuInventory(inventory)) {
            return null;
        }
        return ((MenuHolder) inventory.getHolder()).getMenuId();
    }

    public void handleClick(final Player player, final Inventory inventory, final int rawSlot, final MenuClickType clickType) {
        final String menuId = getMenuId(inventory);
        final ConfiguredMenu menu = getMenu(menuId);
        if (menu == null || rawSlot < 0 || rawSlot >= menu.getSize()) {
            return;
        }
        final MenuItemDefinition item = findItemForSlot(player, menu, rawSlot);
        if (item == null) {
            return;
        }
        if (!clickCooldowns.tryAcquire(player.getUniqueId(), menu.getId(), System.currentTimeMillis(), menu.getClickCooldownMillis())) {
            return;
        }
        if (!canClickPermission(player, item)) {
            messageService.send(player, settings.getMessage("no-permission", "&cYou do not have permission to do that."), menuId);
            soundService.playConfigured(player, "deny");
            return;
        }
        final RequirementGroup clickRequirement = item.getClickRequirement(clickType);
        if (!clickRequirement.passes(player, menuId)) {
            clickRequirement.executeDeny(player, menuId, actionExecutor);
            soundService.playConfigured(player, "deny");
            return;
        }
        soundService.playConfigured(player, "click");
        final List<String> commands = item.getClickCommands(clickType);
        if (item.isCloseOnClick() && !commands.contains("[close]")) {
            actionExecutor.execute(player, menuId, "[close]");
        }
        actionExecutor.executeAll(player, menuId, commands);
    }

    private static boolean canOpen(final Player player, final ConfiguredMenu menu) {
        if (player == null || menu == null) {
            return false;
        }
        final String permission = menu.getPermission();
        return permission == null || permission.trim().isEmpty() || player.hasPermission(permission.trim());
    }

    public void closeAllOpenMenus() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (sessions.containsKey(player.getUniqueId())) {
                FoliaAPI.runTaskForEntity(player, new Runnable() {
                    @Override
                    public void run() {
                        player.closeInventory();
                    }
                });
            }
        }
    }

    private void openNow(final Player player, final ConfiguredMenu menu, final MenuOpenContext context) {
        final String title = messageService.render(player, menu.getTitle(), menu.getId());
        final Inventory inventory = Bukkit.createInventory(new MenuHolder(menu.getId()), menu.getSize(), title);
        render(inventory, player, menu, context);
        player.openInventory(inventory);
        final OpenMenuSession session = new OpenMenuSession(menu.getId(), context);
        sessions.put(player.getUniqueId(), session);
        if (menu.getUpdateIntervalTicks() > 0) {
            scheduleUpdate(player, menu, session, context);
        }
        actionExecutor.executeAll(player, menu.getId(), menu.getOpenActions());
        soundService.playConfigured(player, "open");
    }

    private void renderIntoCurrentInventory(final Player player, final ConfiguredMenu menu, final MenuOpenContext context) {
        final Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!isMenuInventory(inventory)) {
            return;
        }
        render(inventory, player, menu, context);
        player.updateInventory();
    }

    private void render(final Inventory inventory, final Player player, final ConfiguredMenu menu, final MenuOpenContext context) {
        final int size = menu.getSize();
        inventory.clear();
        for (int slot = 0; slot < size; slot++) {
            final MenuItemDefinition item = findItemForSlot(player, menu, slot);
            if (item == null) {
                continue;
            }
            final ItemStack stack = itemBuilder.build(player, menu.getId(), context, item);
            inventory.setItem(slot, stack);
        }
    }

    private MenuItemDefinition findItemForSlot(final Player player, final ConfiguredMenu menu, final int slot) {
        MenuItemDefinition selected = null;
        for (final MenuItemDefinition item : menu.getItemsForSlot(slot)) {
            if (!item.getViewRequirement().passes(player, menu.getId())) {
                continue;
            }
            selected = item;
        }
        return selected;
    }

    private static boolean canClickPermission(final Player player, final MenuItemDefinition item) {
        if (player == null || item == null) {
            return false;
        }
        final String permission = item.getClickPermission();
        return permission == null || permission.trim().isEmpty() || player.hasPermission(permission.trim());
    }

    private void scheduleUpdate(final Player player, final ConfiguredMenu menu, final OpenMenuSession session, final MenuOpenContext context) {
        final long period = Math.max(1L, menu.getUpdateIntervalTicks());
        final Object handle = FoliaAPI.runTaskForEntityRepeatingHandle(player, new java.util.function.Consumer<Object>() {
            @Override
            public void accept(final Object ignored) {
                if (!sessions.containsKey(player.getUniqueId())) {
                    return;
                }
                renderIntoCurrentInventory(player, menu, context);
            }
        }, new Runnable() {
            @Override
            public void run() {
                onClose(player);
            }
        }, period, period);
        session.setUpdateTask(handle);
    }
}
