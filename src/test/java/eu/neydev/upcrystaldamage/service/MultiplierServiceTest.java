package eu.neydev.upcrystaldamage.service;

import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.model.DamageDecision;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Расчет урона: множитель, обход правом и отмена удара при нуле.
 */
class MultiplierServiceTest {

    private final Player victim = mock(Player.class);

    private MultiplierService service(StubConfig config) {
        return new MultiplierService(config, new PermissionService(config));
    }

    @Test
    @DisplayName("Множитель применяется к ванильному урону")
    void multiplierIsApplied() {

        DamageDecision decision = service(new StubConfig().withMultiplier(4.0D)).decide(10.0D, victim);

        assertEquals(40.0D, decision.finalDamage());
        assertEquals(4.0D, decision.multiplier());
        assertEquals(10.0D, decision.baseDamage());
        assertFalse(decision.bypassed());
        assertFalse(decision.cancelled());

    }

    @Test
    @DisplayName("Множитель меньше единицы гасит урон")
    void multiplierBelowOneReducesDamage() {

        DamageDecision decision = service(new StubConfig().withMultiplier(0.1D)).decide(20.0D, victim);

        assertEquals(2.0D, decision.finalDamage(), 1.0E-9);
        assertFalse(decision.cancelled());

    }

    @Test
    @DisplayName("Право обхода оставляет ванильный урон")
    void bypassKeepsVanillaDamage() {

        StubConfig config = new StubConfig()
                .withMultiplier(4.0D)
                .withPermissionsEnabled(true);
        when(victim.hasPermission(config.getPermission(PermissionNode.BYPASS))).thenReturn(true);

        DamageDecision decision = service(config).decide(10.0D, victim);

        assertEquals(10.0D, decision.finalDamage());
        assertEquals(1.0D, decision.multiplier());
        assertTrue(decision.bypassed());
        assertFalse(decision.cancelled());

    }

    @Test
    @DisplayName("Нулевой множитель отменяет удар")
    void zeroMultiplierCancelsHit() {

        DamageDecision decision = service(new StubConfig().withMultiplier(0.0D)).decide(10.0D, victim);

        assertTrue(decision.cancelled());
        assertEquals(0.0D, decision.finalDamage());

    }

    @Test
    @DisplayName("Отрицательный итог тоже трактуется как отмена")
    void negativeResultCancelsHit() {

        DamageDecision decision = service(new StubConfig().withMultiplier(0.5D)).decide(-10.0D, victim);

        assertTrue(decision.cancelled(), "лечить кристаллом плагин не должен");
        assertEquals(-10.0D, decision.baseDamage());

    }

    @Test
    @DisplayName("Единичный множитель ничего не меняет, но обходом не считается")
    void neutralMultiplierIsNotABypass() {

        DamageDecision decision = service(new StubConfig().withMultiplier(1.0D)).decide(7.0D, victim);

        assertEquals(7.0D, decision.finalDamage());
        assertFalse(decision.bypassed());
        assertFalse(decision.cancelled());

    }

}
