package org.ney.crystal_damage;

import org.bukkit.plugin.java.JavaPlugin;
import org.ney.crystal_damage.config.ConfigManager;
import org.ney.crystal_damage.event.EventDispatcher;
import org.ney.crystal_damage.listener.DamageListener;
import org.ney.crystal_damage.service.MultiplierService;

public class NeyUPCrystalDamage extends JavaPlugin {

    private ConfigManager configManager;
    private MultiplierService multiplierService;

    @Override
    public void onEnable() {

        this.configManager = new ConfigManager(this);

        this.multiplierService = new MultiplierService(
                configManager
        );

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(
                new DamageListener(
                        configManager,
                        multiplierService
                )
        );

        getLogger().info("NeyUPCrystalDamage успешно запущен!");

    }

    @Override
    public void onDisable() {
        getLogger().info("NeyUPCrystalDamage остановлен!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}