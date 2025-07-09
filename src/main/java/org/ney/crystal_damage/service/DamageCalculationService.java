package org.ney.crystal_damage.service;

import org.ney.crystal_damage.config.ConfigManager;

public class DamageCalculationService {

    private final ConfigManager configManager;

    public DamageCalculationService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Рассчитывает финальный урон по формуле: base * multiplier
     */
    public double calculateDamage(double baseDamage) {
        return baseDamage * configManager.getDamageMultiplier();
    }
}