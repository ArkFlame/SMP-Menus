package com.arkflame.smpmenus.requirement;

import com.arkflame.smpmenus.action.MenuActionExecutor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;

public final class RequirementGroup {
    private final List<Requirement> requirements;
    private final List<String> denyCommands;

    public RequirementGroup(final List<Requirement> requirements, final List<String> denyCommands) {
        this.requirements = Collections.unmodifiableList(new ArrayList<Requirement>(requirements));
        this.denyCommands = Collections.unmodifiableList(new ArrayList<String>(denyCommands));
    }

    public static RequirementGroup empty() {
        return new RequirementGroup(Collections.<Requirement>emptyList(), Collections.<String>emptyList());
    }

    public boolean passes(final Player player, final String menuId) {
        for (final Requirement requirement : requirements) {
            if (!requirement.passes(player, menuId)) {
                return false;
            }
        }
        return true;
    }

    public void executeDeny(final Player player, final String menuId, final MenuActionExecutor executor) {
        executor.executeAll(player, menuId, denyCommands);
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }
}
