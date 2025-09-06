package org.ney.crystal_damage.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.ney.crystal_damage.config.ConfigManager;

public class MultiplierService {

    private final ConfigManager configManager;

    public MultiplierService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public double calculateDamage(double baseDamage, @NotNull Entity damager) {

        Player owner = Bukkit.getPlayer(damager.getUniqueId());
        if (owner != null && hasBypassPermission(owner)) {
            return baseDamage;
        }

        return baseDamage * configManager.getDamageMultiplier();

    }

    private boolean hasBypassPermission(@NotNull Player player) {

        if (!configManager.isBypassPermissionEnabled()) {
            return false;
        }

        return player.hasPermission(configManager.getBypassPermission());

    }
}