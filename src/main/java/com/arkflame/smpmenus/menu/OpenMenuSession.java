package com.arkflame.smpmenus.menu;

import com.arkflame.smpmenus.util.FoliaAPI;

public final class OpenMenuSession {
    private final String menuId;
    private final MenuOpenContext context;
    private Object updateTask;

    public OpenMenuSession(final String menuId, final MenuOpenContext context) {
        this.menuId = menuId;
        this.context = context == null ? MenuOpenContext.of(null, menuId) : context;
    }

    public String getMenuId() {
        return menuId;
    }

    public MenuOpenContext getContext() {
        return context;
    }

    public void setUpdateTask(final Object updateTask) {
        cancelUpdateTask();
        this.updateTask = updateTask;
    }

    public void cancelUpdateTask() {
        if (updateTask != null) {
            FoliaAPI.cancelTask(updateTask);
            updateTask = null;
        }
    }
}