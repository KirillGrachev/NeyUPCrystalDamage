package eu.neydev.upcrystaldamage.listener;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.service.MultiplierService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Слушатель урона кристалла Края по игроку.
 *
 * Приоритет HIGHEST с ignoreCancelled: решение принимается после плагинов,
 * меняющих урон (защита регионов, античит, балансировка PvP), но до MONITOR,
 * который читают логгеры и статистика, - иначе они увидели бы ванильное
 * значение. Уже отмененный удар не пересчитывается вовсе.
 */
public class DamageListener implements Listener {

    private final CrystalDamageConfig config;
    private final MultiplierService multiplierService;

    public DamageListener(@NotNull CrystalDamageConfig config,
                          @NotNull MultiplierService multiplierService) {
        this.config = config;
        this.multiplierService = multiplierService;
    }

    /**
     * Пересчитывает урон кристалла по игроку.
     *
     * @param event событие урона сущности другой сущностью
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {

        if (!config.isPluginEnabled()) {
            return;
        }

        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (damager.getType() != EntityType.END_CRYSTAL || !(damaged instanceof Player)) {
            return;
        }

        Player player = (Player) damaged;

        // Предупреждение уходит до расчета урона: игрок видит его и тогда,
        // когда удар в итоге отменен нулевым множителем
        for (String line : config.getMessages(MessageKey.MESSAGE_BEFORE_DAMAGE)) {
            player.sendMessage(line);
        }

        double finalDamage = multiplierService.calculateDamage(event.getDamage(), damager);

        // Ноль отменяет удар целиком, а не оставляет событие с нулевым уроном:
        // иначе смерть от кристалла продолжает числиться смертью от взрыва
        if (finalDamage == 0.0D) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(finalDamage);

    }

}
