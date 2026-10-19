package eu.neydev.upcrystaldamage;

import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import eu.neydev.upcrystaldamage.service.MessageService;
import eu.neydev.upcrystaldamage.service.MultiplierService;
import eu.neydev.upcrystaldamage.service.PermissionService;
import org.jetbrains.annotations.NotNull;

/**
 * Контейнер компонентов плагина.
 *
 * Порядок создания зафиксирован: каждый сервис получает уже готовые
 * зависимости и никто не создает их себе сам. Граф маленький, но собран
 * по тем же правилам, что и в остальных плагинах семьи, - добавление
 * сервиса не заставляет править точку входа и не плодит конструкторы
 * с десятком аргументов в слушателе.
 */
public final class ServiceContainer {

    private final NeyUPCrystalDamage plugin;
    private final ConfigManager configManager;
    private final ConsoleService consoleService;

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final MultiplierService multiplierService;

    public ServiceContainer(@NotNull NeyUPCrystalDamage plugin,
                            @NotNull ConfigManager configManager,
                            @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.configManager = configManager;
        this.consoleService = consoleService;

        // Базовый слой: сообщения и права читают только конфигурацию
        this.messageService = new MessageService(configManager);
        this.permissionService = new PermissionService(configManager);

        // Слой урона: зависит от прав, поэтому создается после них
        this.multiplierService = new MultiplierService(configManager, permissionService);

    }

    public NeyUPCrystalDamage getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public MultiplierService getMultiplierService() {
        return multiplierService;
    }

}
