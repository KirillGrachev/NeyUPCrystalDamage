package org.ney.crystal_damage.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

import org.ney.crystal_damage.config.ConfigManager;
import org.ney.crystal_damage.service.MultiplierService;

public class DamageListener implements Listener {

    private final ConfigManager configManager;
    private final MultiplierService multiplierService;

    public DamageListener(@NotNull ConfigManager configManager,
                          @NotNull MultiplierService multiplierService) {
        this.configManager = configManager;
        this.multiplierService = multiplierService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (damager.getType() != EntityType.ENDER_CRYSTAL
                || !(damaged instanceof Player)) {
            return;
        }

        Player player = (Player) damaged;

        if (configManager.isMessageEnabled()) {
            configManager.getMessageLines().forEach(player::sendMessage);
        }

        double baseDamage = event.getDamage();
        double finalDamage = multiplierService.calculateDamage(baseDamage, damager);

        if (finalDamage == 0.0) {

            event.setCancelled(true);
            return;

        }

        event.setDamage(finalDamage);

    }
}