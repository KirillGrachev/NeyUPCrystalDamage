package eu.neydev.upcrystaldamage;

import eu.neydev.upcrystaldamage.command.CommandDispatcher;
import eu.neydev.upcrystaldamage.command.CrystalDamageCommand;
import eu.neydev.upcrystaldamage.command.CrystalDamageSubCommand;
import eu.neydev.upcrystaldamage.command.sub.InfoSubCommand;
import eu.neydev.upcrystaldamage.command.sub.ReloadSubCommand;
import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.event.EventDispatcher;
import eu.neydev.upcrystaldamage.listener.CrystalDamageListener;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Точка входа плагина.
 *
 * Класс не final осознанно: интеграционные тесты (MockBukkit) грузят плагин
 * через прокси-подкласс, а наследование JavaPlugin в продакшене никто не
 * использует - подклассы создаются только тестовой средой.
 */
public class NeyUPCrystalDamage extends JavaPlugin {

    private static final String COMMAND_CRYSTAL_DAMAGE = "crystaldamage";

    private ConsoleService consoleService;
    private ConfigManager configManager;
    private ServiceContainer services;
    private @Nullable CommandDispatcher commandDispatcher;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);
        this.services = new ServiceContainer(this, configManager, consoleService);

        new EventDispatcher(this).registerEvents(new CrystalDamageListener(this,
                configManager,
                services.getMultiplierService(),
                services.getMessageService()));

        this.commandDispatcher = new CommandDispatcher(this, consoleService);
        commandDispatcher.registerCommand(COMMAND_CRYSTAL_DAMAGE, new CrystalDamageCommand(
                services.getMessageService(),
                services.getPermissionService(),
                buildSubCommands()
        ));

        getLogger().info("NeyUPCrystalDamage started successfully.");

    }

    @Override
    public void onDisable() {

        if (commandDispatcher != null) {
            commandDispatcher.unregisterCommand(COMMAND_CRYSTAL_DAMAGE);
        }

        getLogger().info("NeyUPCrystalDamage stopped.");

    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ServiceContainer getServices() {
        return services;
    }

    /**
     * Собирает подкоманды: слушатели и команда получают зависимости из
     * контейнера, а не тянут их из плагина сами.
     */
    private @NotNull List<CrystalDamageSubCommand> buildSubCommands() {
        return List.of(
                new ReloadSubCommand(configManager, services.getMessageService()),
                new InfoSubCommand(this, configManager, services.getMessageService())
        );
    }

}
