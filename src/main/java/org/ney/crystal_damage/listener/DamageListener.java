package org.ney.crystal_damage.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.jetbrains.annotations.NotNull;

import org.ney.crystal_damage.config.ConfigManager;
import org.ney.crystal_damage.service.DamageCalculationService;
import org.ney.crystal_damage.service.DamageValidationService;
import org.ney.crystal_damage.service.ExperienceDropService;

/**
 * Слушатель событий повреждений кристалла.
 * Перехватывает попытки нанесения урона игрокам через эндер-кристаллы.
 */
public class DamageListener implements Listener {

    private final ConfigManager configManager;
    private final DamageValidationService validationService;
    private final DamageCalculationService calculationService;
    private final ExperienceDropService experienceDropService;

    public DamageListener(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
        this.validationService = new DamageValidationService();
        this.calculationService = new DamageCalculationService(configManager);
        this.experienceDropService = new ExperienceDropService();
    }

    /**
     * Обрабатывает нанесение урона от эндер-кристалла.
     *
     * @param event событие EntityDamageByEntityEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {

        Entity damaged = event.getEntity();

        if (!validationService.isCrystalDamageValid(event, damaged)) {
            return;
        }

        Player player = (Player) damaged;

        if (configManager.isMessageEnabled()) {
            configManager.getMessageLines().forEach(player::sendMessage);
        }

        double damage = calculationService.calculateDamage(event.getDamage());
        event.setDamage(damage);

        if (damage == 0) {
            event.setCancelled(true);
            return;
        }

        if (configManager.isExpEnabled()) {
            experienceDropService.spawnExperienceOrb(player, damage);
        }
    }
}