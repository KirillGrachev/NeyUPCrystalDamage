package eu.neydev.upcrystaldamage;

import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.event.EventDispatcher;
import eu.neydev.upcrystaldamage.listener.DamageListener;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import eu.neydev.upcrystaldamage.service.MultiplierService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Точка входа плагина.
 *
 * Класс не final осознанно: интеграционные тесты (MockBukkit) грузят плагин
 * через прокси-подкласс, а наследование JavaPlugin в продакшене никто не
 * использует - подклассы создаются только тестовой средой.
 */
public class NeyUPCrystalDamage extends JavaPlugin {

    private ConsoleService consoleService;
    private ConfigManager configManager;
    private MultiplierService multiplierService;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);
        this.multiplierService = new MultiplierService(configManager);

        // Регистрация слушателей
        new EventDispatcher(this).registerEvents(
                new DamageListener(configManager, multiplierService)
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
