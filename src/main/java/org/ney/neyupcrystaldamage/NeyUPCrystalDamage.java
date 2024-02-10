package org.ney.neyupcrystaldamage;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.ney.neyupcrystaldamage.Listeners.DamageByEntity;

public class NeyUPCrystalDamage extends JavaPlugin implements Listener {

    private static NeyUPCrystalDamage instance;

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        registerListeners(DamageByEntity.class);

    }

    public static NeyUPCrystalDamage getInstance() { return NeyUPCrystalDamage.instance; }

    @SafeVarargs
    public final void registerListeners(Class<? extends Listener>... listenerClasses) {

        for (Class<? extends Listener> listenerClass : listenerClasses) {

            try {

                Listener listener = listenerClass.newInstance();
                Bukkit.getPluginManager().registerEvents(listener, NeyUPCrystalDamage.getInstance());

            } catch (InstantiationException | IllegalAccessException e) { e.printStackTrace(); }
        }
    }
}