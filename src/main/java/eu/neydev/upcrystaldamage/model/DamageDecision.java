package eu.neydev.upcrystaldamage.model;

import org.jetbrains.annotations.NotNull;

/**
 * Решение по урону кристалла.
 *
 * Три исхода различаются флагами, а не наследованием: слушателю нужен один
 * тип с понятными полями, а не цепочка instanceof. Множитель хранится и для
 * обхода (1.0), чтобы событие и диагностика показывали, что именно применено.
 *
 * @param baseDamage  ванильный урон до вмешательства плагина
 * @param multiplier  примененный множитель
 * @param finalDamage итоговый урон; при обходе равен ванильному, при отмене - 0
 * @param bypassed    урон не менялся: у игрока есть право обхода
 * @param cancelled   удар отменяется целиком (итоговый урон нулевой)
 */
public record DamageDecision(double baseDamage,
                             double multiplier,
                             double finalDamage,
                             boolean bypassed,
                             boolean cancelled) {

    /**
     * Урон не тронут: игрок обходит множитель правом.
     *
     * @param baseDamage ванильный урон события
     * @return решение с множителем 1.0
     */
    public static @NotNull DamageDecision bypassed(double baseDamage) {
        return new DamageDecision(baseDamage, 1.0D, baseDamage, true, false);
    }

    /**
     * Удар отменяется: итоговый урон нулевой.
     *
     * @param baseDamage ванильный урон события
     * @param multiplier множитель, давший ноль
     * @return решение с признаком отмены
     */
    public static @NotNull DamageDecision cancelled(double baseDamage, double multiplier) {
        return new DamageDecision(baseDamage, multiplier, 0.0D, false, true);
    }

    /**
     * Урон пересчитан множителем.
     *
     * @param baseDamage  ванильный урон события
     * @param multiplier  примененный множитель
     * @param finalDamage итоговый урон
     * @return решение с новым уроном
     */
    public static @NotNull DamageDecision multiplied(double baseDamage,
                                                     double multiplier,
                                                     double finalDamage) {
        return new DamageDecision(baseDamage, multiplier, finalDamage, false, false);
    }

}
