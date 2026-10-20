package eu.neydev.upcrystaldamage.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Три исхода решения по урону: обход правом, отмена удара и пересчет.
 */
class DamageDecisionTest {

    @Test
    @DisplayName("Обход правом оставляет ванильный урон и множитель 1.0")
    void bypassKeepsVanillaDamage() {

        DamageDecision decision = DamageDecision.bypassed(12.5D);

        assertEquals(12.5D, decision.baseDamage());
        assertEquals(1.0D, decision.multiplier());
        assertEquals(12.5D, decision.finalDamage());
        assertTrue(decision.bypassed());
        assertFalse(decision.cancelled());

    }

    @Test
    @DisplayName("Отмена обнуляет итоговый урон и помечает удар")
    void cancelledZeroesDamage() {

        DamageDecision decision = DamageDecision.cancelled(20.0D, 0.0D);

        assertEquals(20.0D, decision.baseDamage());
        assertEquals(0.0D, decision.multiplier());
        assertEquals(0.0D, decision.finalDamage());
        assertFalse(decision.bypassed());
        assertTrue(decision.cancelled());

    }

    @Test
    @DisplayName("Пересчет хранит все три значения")
    void multipliedKeepsEveryValue() {

        DamageDecision decision = DamageDecision.multiplied(10.0D, 4.0D, 40.0D);

        assertEquals(10.0D, decision.baseDamage());
        assertEquals(4.0D, decision.multiplier());
        assertEquals(40.0D, decision.finalDamage());
        assertFalse(decision.bypassed());
        assertFalse(decision.cancelled());

    }

    @Test
    @DisplayName("Record сравнивается по значениям")
    void recordEqualityIsStructural() {

        assertEquals(DamageDecision.multiplied(1.0D, 2.0D, 2.0D),
                DamageDecision.multiplied(1.0D, 2.0D, 2.0D));
        assertNotEquals(DamageDecision.multiplied(1.0D, 2.0D, 2.0D),
                DamageDecision.bypassed(1.0D));
        assertTrue(DamageDecision.bypassed(3.0D).toString().contains("3.0"));

    }

}
