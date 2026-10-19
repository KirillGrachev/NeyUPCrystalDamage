package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Сервис прав доступа.
 *
 * Два класса узлов живут по противоположным правилам, и путать их нельзя:
 * функциональные (reload, info) при выключенной системе прав разрешены всем,
 * а ограничивающие (bypass) - никому. «Права выключены» означает «функции
 * доступны всем», но никак не «ограничения сняты со всех»: иначе одна строка
 * в config.yml молча лишала бы донатеров купленного обхода урона.
 */
public class PermissionService {

    private final CrystalDamageConfig config;

    public PermissionService(@NotNull CrystalDamageConfig config) {
        this.config = config;
    }

    /**
     * Проверяет право на действие (команда, справка, диагностика).
     *
     * @param sender проверяемый отправитель (игрок или консоль)
     * @param node   узел права
     * @return true если действие разрешено
     */
    public boolean has(@Nullable CommandSender sender, @NotNull PermissionNode node) {

        if (!config.arePermissionsEnabled()) {
            return true;
        }

        return sender != null && sender.hasPermission(config.getPermission(node));

    }

    /**
     * Проверяет право на обход множителя урона.
     *
     * @param player игрок, получающий урон кристалла
     * @return true если урон остается ванильным
     */
    public boolean hasBypass(@Nullable Player player) {

        if (!config.arePermissionsEnabled()) {
            return false;
        }

        return player != null && player.hasPermission(config.getPermission(PermissionNode.BYPASS));

    }

}
