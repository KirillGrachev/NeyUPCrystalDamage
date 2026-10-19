package eu.neydev.upcrystaldamage.listener;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.event.CrystalDamageMultiplyEvent;
import eu.neydev.upcrystaldamage.model.DamageDecision;
import eu.neydev.upcrystaldamage.service.MessageService;
import eu.neydev.upcrystaldamage.service.MultiplierService;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Слушатель урона кристалла Края по игроку.
 *
 * Приоритет HIGHEST с ignoreCancelled: решение принимается после плагинов,
 * меняющих урон (защита регионов, античит, балансировка PvP), но до MONITOR,
 * который читают логгеры и статистика, - иначе они увидели бы ванильное
 * значение. Уже отмененный удар не пересчитывается вовсе.
 *
 * Источник урона определяется по типу {@link EnderCrystal}, а не по
 * {@code EntityType.END_CRYSTAL}: константу переименовали в 1.20.5, и
 * обращение к ней уронило бы плагин с NoSuchFieldError на ядрах 1.20-1.20.4,
 * которые api-version '1.20' формально разрешает.
 */
public class CrystalDamageListener implements Listener {

    private final Plugin plugin;
    private final CrystalDamageConfig config;
    private final MultiplierService multiplierService;
    private final MessageService messageService;

    public CrystalDamageListener(@NotNull Plugin plugin,
                                 @NotNull CrystalDamageConfig config,
                                 @NotNull MultiplierService multiplierService,
                                 @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.multiplierService = multiplierService;
        this.messageService = messageService;
    }

    /**
     * Пересчитывает урон кристалла по игроку.
     *
     * @param event событие урона сущности другой сущностью
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrystalDamage(@NotNull EntityDamageByEntityEvent event) {

        if (!config.isPluginEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof EnderCrystal crystal)) {
            return;
        }

        // Предупреждение уходит до расчета урона: игрок видит его и тогда,
        // когда удар в итоге отменен нулевым множителем
        messageService.send(victim, MessageKey.MESSAGE_BEFORE_DAMAGE);

        DamageDecision decision = multiplierService.decide(event.getDamage(), victim);

        CrystalDamageMultiplyEvent multiplyEvent =
                new CrystalDamageMultiplyEvent(victim, crystal, decision);
        plugin.getServer().getPluginManager().callEvent(multiplyEvent);

        if (multiplyEvent.isCancelled()) {
            return;
        }

        if (decision.cancelled()) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(multiplyEvent.getDamage());

    }

}
