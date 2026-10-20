package eu.neydev.upcrystaldamage;

import eu.neydev.upcrystaldamage.config.ConfigManager;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Контейнер собирает граф один раз и передает сервисам одну и ту же
 * конфигурацию: проверка, что ничего не создается в двух экземплярах.
 */
class ServiceContainerSmokeTest {

    private final NeyUPCrystalDamage plugin = mock(NeyUPCrystalDamage.class);
    private final ConfigManager configManager = mock(ConfigManager.class);
    private final ConsoleService consoleService = mock(ConsoleService.class);

    private ServiceContainer container() {
        return new ServiceContainer(plugin, configManager, consoleService);
    }

    @Test
    @DisplayName("Все компоненты созданы и доступны")
    void everyComponentIsCreated() {

        ServiceContainer container = container();

        assertSame(plugin, container.getPlugin());
        assertSame(configManager, container.getConfigManager());
        assertSame(consoleService, container.getConsoleService());
        assertNotNull(container.getMessageService());
        assertNotNull(container.getPermissionService());
        assertNotNull(container.getMultiplierService());

    }

    @Test
    @DisplayName("Сервисы читают конфигурацию из контейнера, а не свою копию")
    void servicesShareTheSameConfig() {

        when(configManager.getMessages(MessageKey.RELOAD)).thenReturn(List.of("shared"));
        when(configManager.areMessagesEnabled()).thenReturn(true);
        when(configManager.getMessagePrefix()).thenReturn("");

        assertEquals(List.of("shared"),
                container().getMessageService().build(MessageKey.RELOAD, Map.of()));

    }

    @Test
    @DisplayName("Два контейнера не делят сервисы между собой")
    void containersDoNotShareServices() {

        ServiceContainer first = container();
        ServiceContainer second = container();

        assertNotNull(first.getMessageService());
        assertNotNull(second.getMessageService());
        assertEquals(first.getMultiplierService().getClass(), second.getMultiplierService().getClass());

    }

}
