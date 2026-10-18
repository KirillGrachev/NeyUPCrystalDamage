package eu.neydev.upcrystaldamage;

import org.bukkit.plugin.java.JavaPlugin;
import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.event.EventDispatcher;
import eu.neydev.upcrystaldamage.listener.DamageListener;
import eu.neydev.upcrystaldamage.service.MultiplierService;

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

        getLogger().info("NeyUPCrystalDamage started successfully.");

    }

    @Override
    public void onDisable() {
        getLogger().info("NeyUPCrystalDamage stopped.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}