package eu.neydev.upcrystaldamage.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import eu.neydev.upcrystaldamage.NeyUPCrystalDamage;

public class EventDispatcher {

    private final NeyUPCrystalDamage plugin;

    public EventDispatcher(NeyUPCrystalDamage plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }
}