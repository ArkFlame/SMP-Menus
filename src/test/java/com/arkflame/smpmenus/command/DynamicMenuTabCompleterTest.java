package com.arkflame.smpmenus.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class DynamicMenuTabCompleterTest {

    private DynamicMenuTabCompleter createTestCompleter(LinkedHashSet<String> menuIds, List<String> playerNames) {
        return new DynamicMenuTabCompleter(() -> menuIds, () -> playerNames);
    }

    @Test
    public void filtersAdminSubcommands() {
        final DynamicMenuTabCompleter completer = createTestCompleter(
                new LinkedHashSet<String>(Arrays.asList("help", "rules")),
                Arrays.asList("Alice", "Bob"));
        final List<String> result = completer.completeAdmin(null, new String[] {"re"});
        Assertions.assertEquals(Collections.singletonList("reload"), result);
    }

    @Test
    public void completesMenuIdsForOpenSubcommand() {
        final DynamicMenuTabCompleter completer = createTestCompleter(
                new LinkedHashSet<String>(Arrays.asList("help", "rules")),
                Arrays.asList("Alice", "Bob"));
        final List<String> result = completer.completeAdmin(null, new String[] {"open", "r"});
        Assertions.assertEquals(Collections.singletonList("rules"), result);
    }

    @Test
    public void returnsEmptyForMenuOpenCommandArguments() {
        final DynamicMenuTabCompleter completer = createTestCompleter(
                new LinkedHashSet<String>(Arrays.asList("help", "rules")),
                Arrays.asList("Alice", "Bob"));
        final List<String> result = completer.completeMenuOpen(null, new String[] {"anything"});
        Assertions.assertTrue(result.isEmpty());
    }
}