package eu.neydev.upcrystaldamage.event;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Диспетчер регистрации слушателей событий.
 *
 * Зависит от {@link Plugin}, а не от конкретного класса плагина: регистрация
 * проверяется на моке сервера без загрузки всего плагина.
 */
public class EventDispatcher {

    private final Plugin plugin;

    public EventDispatcher(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует переданные слушатели в Bukkit.
     *
     * @param listeners слушатели событий
     */
    public void registerEvents(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

}
