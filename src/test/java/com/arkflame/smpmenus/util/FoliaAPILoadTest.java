package com.arkflame.smpmenus.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

final class FoliaAPILoadTest {

    @Test
    void classInitializationDoesNotThrowWithoutServerBootstrap() {
        assertDoesNotThrow(() -> Class.forName("com.arkflame.smpmenus.util.FoliaAPI"));
    }

    @Test
    void cancelAllTasksBeforeInitDoesNotThrow() {
        assertDoesNotThrow(FoliaAPI::cancelAllTasks);
    }
}