package com.arkflame.smpmenus.requirement;

import com.arkflame.smpmenus.hook.PlaceholderHook;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

public final class RequirementFactory {
    private final PlaceholderHook placeholderHook;
    private final Logger logger;

    public RequirementFactory(final PlaceholderHook placeholderHook, final Logger logger) {
        this.placeholderHook = placeholderHook;
        this.logger = logger;
    }

    public RequirementGroup load(final ConfigurationSection section) {
        if (section == null) {
            return RequirementGroup.empty();
        }
        final List<Requirement> requirements = new ArrayList<Requirement>();
        final ConfigurationSection requirementSection = section.getConfigurationSection("requirements");
        if (requirementSection != null) {
            for (final String key : requirementSection.getKeys(false)) {
                final ConfigurationSection child = requirementSection.getConfigurationSection(key);
                if (child == null) {
                    continue;
                }
                final Requirement requirement = loadSingle(child);
                if (requirement != null) {
                    requirements.add(requirement);
                }
            }
        }
        return new RequirementGroup(requirements, section.getStringList("deny_commands"));
    }

    private Requirement loadSingle(final ConfigurationSection section) {
        final String type = section.getString("type", "").trim().toLowerCase(Locale.ROOT);
        if ("has permission".equals(type) || "has_permission".equals(type)) {
            return new PermissionRequirement(section.getString("permission", ""), true);
        }
        if ("!has permission".equals(type) || "does not have permission".equals(type) || "lacks permission".equals(type)) {
            return new PermissionRequirement(section.getString("permission", ""), false);
        }
        if ("string equals ignorecase".equals(type) || "string equals ignore case".equals(type)) {
            return new StringRequirement(placeholderHook, section.getString("input", ""), section.getString("output", ""), true);
        }
        if ("string equals".equals(type)) {
            return new StringRequirement(placeholderHook, section.getString("input", ""), section.getString("output", ""), false);
        }
        if (">=".equals(type) || ">".equals(type) || "<=".equals(type) || "<".equals(type) || "==".equals(type) || "=".equals(type) || "!=".equals(type)) {
            final String operator = "=".equals(type) ? "==" : type;
            return new NumericRequirement(placeholderHook, operator, section.getString("input", "0"), section.getString("output", "0"));
        }
        if ("has item".equals(type) || "has_item".equals(type)) {
            return new HasItemRequirement(section.getString("material", section.getString("item", "AIR")), section.getInt("amount", 1), section.getInt("data", -1));
        }
        logger.warning("Unknown menu requirement type: " + type + " at " + section.getCurrentPath() + "; requirement fails closed.");
        return FalseRequirement.INSTANCE;
    }
}
