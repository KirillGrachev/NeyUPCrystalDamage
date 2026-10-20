package eu.neydev.upcrystaldamage.event;

import eu.neydev.upcrystaldamage.model.DamageDecision;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Контракты собственного события: синхронность, свой HandlerList,
 * отмена и перехват итогового урона.
 */
class CrystalDamageMultiplyEventTest {

    private final Player victim = mock(Player.class);
    private final EnderCrystal crystal = mock(EnderCrystal.class);

    private CrystalDamageMultiplyEvent event(DamageDecision decision) {
        return new CrystalDamageMultiplyEvent(victim, crystal, decision);
    }

    @Test
    @DisplayName("Событие синхронное и несет свои значения")
    void eventIsSynchronousAndCarriesValues() {

        DamageDecision decision = DamageDecision.multiplied(10.0D, 4.0D, 40.0D);
        CrystalDamageMultiplyEvent event = event(decision);

        assertFalse(event.isAsynchronous(), "кристалл бьет в главном потоке");
        assertSame(victim, event.getVictim());
        assertSame(crystal, event.getCrystal());
        assertSame(decision, event.getDecision());
        assertEquals(40.0D, event.getDamage(), "стартовый урон - из решения");

    }

    @Test
    @DisplayName("HandlerList собственный и совпадает со статическим")
    void handlerListIsOwnAndStatic() {

        HandlerList handlers = event(DamageDecision.bypassed(1.0D)).getHandlers();

        assertSame(CrystalDamageMultiplyEvent.getHandlerList(), handlers);
        assertSame(handlers, event(DamageDecision.bypassed(2.0D)).getHandlers());

    }

    @Test
    @DisplayName("Отмена по умолчанию выключена и переключается")
    void cancellationDefaultsToFalse() {

        CrystalDamageMultiplyEvent event = event(DamageDecision.bypassed(1.0D));

        assertFalse(event.isCancelled());

        event.setCancelled(true);

        assertTrue(event.isCancelled());

    }

    @Test
    @DisplayName("Урон можно перехватить, отрицательный прижимается к нулю")
    void damageCanBeOverriddenAndClamped() {

        CrystalDamageMultiplyEvent event = event(DamageDecision.multiplied(10.0D, 4.0D, 40.0D));

        event.setDamage(7.5D);
        assertEquals(7.5D, event.getDamage());

        event.setDamage(-3.0D);
        assertEquals(0.0D, event.getDamage(), "отрицательный урон не лечит игрока");

    }

}
