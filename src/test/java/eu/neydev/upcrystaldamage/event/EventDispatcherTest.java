package eu.neydev.upcrystaldamage.event;

import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Регистрация слушателей идет через PluginManager плагина, а не через
 * статический Bukkit: диспетчер проверяется без поднятого сервера.
 */
class EventDispatcherTest {

    private final Plugin plugin = mock(Plugin.class);
    private final Server server = mock(Server.class);
    private final PluginManager pluginManager = mock(PluginManager.class);

    @BeforeEach
    void setUp() {
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
    }

    @Test
    @DisplayName("Все переданные слушатели регистрируются от имени плагина")
    void registersEveryListener() {

        Listener first = mock(Listener.class);
        Listener second = mock(Listener.class);

        new EventDispatcher(plugin).registerEvents(first, second);

        verify(pluginManager).registerEvents(first, plugin);
        verify(pluginManager).registerEvents(second, plugin);
        verifyNoMoreInteractions(pluginManager);

    }

    @Test
    @DisplayName("Пустой список не трогает PluginManager")
    void emptyCallIsNoOp() {

        new EventDispatcher(plugin).registerEvents();

        verify(pluginManager, times(0)).registerEvents(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

    }

}
