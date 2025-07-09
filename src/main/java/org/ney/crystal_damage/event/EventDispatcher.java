package org.ney.crystal_damage.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import org.ney.crystal_damage.NeyUPCrystalDamage;

/**
 * Диспетчер событий для регистрации обработчиков.
 */
public class EventDispatcher {

    private final NeyUPCrystalDamage plugin;

    public EventDispatcher(NeyUPCrystalDamage plugin) {
        this.plugin = plugin;
    }

    /**
     * Регистрирует один или несколько слушателей событий.
     *
     * @param listeners массив слушателей для регистрации
     */
    public void registerEvents(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }
}