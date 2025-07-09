package org.ney.crystal_damage.service;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class DamageValidationService {

    /**
     * Проверяет, является ли урон корректным:
     * - Источник урона — эндер-кристалл
     * - Цель — игрок
     */
    public boolean isCrystalDamageValid(@NotNull EntityDamageByEntityEvent event,
                                        Entity damaged) {

        Entity damager = event.getDamager();

        return damager.getType() == EntityType.ENDER_CRYSTAL
                && damaged instanceof Player;

    }
}