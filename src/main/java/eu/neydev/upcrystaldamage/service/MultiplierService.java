package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис расчета урона кристалла Края.
 */
public class MultiplierService {

    private final CrystalDamageConfig config;

    public MultiplierService(@NotNull CrystalDamageConfig config) {
        this.config = config;
    }

    /**
     * Считает итоговый урон кристалла.
     *
     * @param baseDamage ванильный урон события
     * @param damager    источник урона
     * @return урон к применению
     */
    public double calculateDamage(double baseDamage, @NotNull Entity damager) {

        Player owner = Bukkit.getPlayer(damager.getUniqueId());

        if (owner != null && hasBypassPermission(owner)) {
            return baseDamage;
        }

        return baseDamage * config.getDamageMultiplier();

    }

    private boolean hasBypassPermission(@NotNull Player player) {

        if (!config.arePermissionsEnabled()) {
            return false;
        }

        return player.hasPermission(config.getPermission(PermissionNode.BYPASS));

    }

}
