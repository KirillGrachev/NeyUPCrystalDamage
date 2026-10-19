package eu.neydev.upcrystaldamage.event;

import eu.neydev.upcrystaldamage.model.DamageDecision;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Вызывается перед применением множителя к урону кристалла Края.
 *
 * Два контракта Bukkit соблюдены явно:
 * 1. Событие синхронное: конструктор не передает флаг async, иначе вызов
 *    из главного потока падает с "may only be triggered asynchronously".
 * 2. HandlerList собственный и статический: общий список приводил бы к тому,
 *    что слушатель одного события получал бы все события плагина.
 *
 * Отмена означает «не трогать ванильный урон»: плагин не применяет множитель
 * и не отменяет удар - решение остается за тем, кто отменил событие.
 * {@link #setDamage} перехватывает итоговое значение без отмены: античитам
 * и плагинам защиты региона не приходится пересчитывать урон самим.
 */
public class CrystalDamageMultiplyEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player victim;
    private final EnderCrystal crystal;
    private final DamageDecision decision;

    private double damage;
    private boolean cancelled;

    public CrystalDamageMultiplyEvent(@NotNull Player victim,
                                      @NotNull EnderCrystal crystal,
                                      @NotNull DamageDecision decision) {
        this.victim = victim;
        this.crystal = crystal;
        this.decision = decision;
        this.damage = decision.finalDamage();
    }

    /**
     * Игрок, получающий урон.
     */
    public @NotNull Player getVictim() {
        return victim;
    }

    /**
     * Кристалл Края - источник урона.
     */
    public @NotNull EnderCrystal getCrystal() {
        return crystal;
    }

    /**
     * Решение плагина: множитель, признак обхода правом и признак отмены.
     */
    public @NotNull DamageDecision getDecision() {
        return decision;
    }

    /**
     * Урон, который будет применен к событию.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Подменяет итоговый урон.
     *
     * Отрицательные значения прижимаются к нулю: урон ниже нуля Bukkit
     * трактует как лечение, и кристалл, «лечащий» игрока, почти наверняка
     * ошибка вызывающей стороны, а не намерение.
     *
     * @param damage новый итоговый урон
     */
    public void setDamage(double damage) {
        this.damage = Math.max(0.0D, damage);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
