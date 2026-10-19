package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.model.DamageDecision;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис расчета урона кристалла Края.
 *
 * Исход возвращается значением ({@link DamageDecision}), а не применяется
 * здесь же: расчет не знает про Bukkit-событие, поэтому его можно проверить
 * без сервера, а слушатель не пересчитывает семантику заново и не угадывает,
 * почему удар отменен - из-за нулевого множителя или из-за права обхода.
 */
public class MultiplierService {

    private final CrystalDamageConfig config;
    private final PermissionService permissionService;

    public MultiplierService(@NotNull CrystalDamageConfig config,
                             @NotNull PermissionService permissionService) {
        this.config = config;
        this.permissionService = permissionService;
    }

    /**
     * Считает итоговый урон кристалла для пострадавшего игрока.
     *
     * Право обхода проверяется по пострадавшему, а не по источнику урона:
     * версия 1.x искала игрока по UUID самого кристалла
     * ({@code Bukkit.getPlayer(damager.getUniqueId())}), что не совпадало
     * никогда - у кристалла нет владельца-игрока, и bypass-permission
     * оставался нерабочим при любом значении permissions.enabled.
     *
     * @param baseDamage ванильный урон события
     * @param victim     игрок, получающий урон
     * @return решение: множитель, итоговый урон, признаки обхода и отмены
     */
    public @NotNull DamageDecision decide(double baseDamage, @NotNull Player victim) {

        if (permissionService.hasBypass(victim)) {
            return DamageDecision.bypassed(baseDamage);
        }

        double multiplier = config.getDamageMultiplier();
        double damage = baseDamage * multiplier;

        // Ноль отменяет удар целиком, а не оставляет событие с нулевым уроном:
        // иначе смерть от кристалла продолжает числиться смертью от взрыва
        // с нулевым уроном, и ее видят и логгеры, и плагины статистики
        if (damage <= 0.0D) {
            return DamageDecision.cancelled(baseDamage, multiplier);
        }

        return DamageDecision.multiplied(baseDamage, multiplier, damage);

    }

}
