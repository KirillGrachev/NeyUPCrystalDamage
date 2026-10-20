package eu.neydev.upcrystaldamage.listener;

import be.seeseemelk.mockbukkit.MockBukkit;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.event.CrystalDamageMultiplyEvent;
import eu.neydev.upcrystaldamage.model.DamageDecision;
import eu.neydev.upcrystaldamage.service.MessageService;
import eu.neydev.upcrystaldamage.service.MultiplierService;
import eu.neydev.upcrystaldamage.service.PermissionService;
import eu.neydev.upcrystaldamage.support.StubConfig;
import org.bukkit.Server;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Поведение слушателя на настоящем Bukkit-событии: пересчет урона, отмена,
 * обход правом, чужие источники урона и взаимодействие с собственным
 * событием плагина.
 *
 * Сервер мокается целиком, а не только плагин: конструктор
 * EntityDamageByEntityEvent в 1.20.6 обращается к Bukkit.getUnsafe()
 * за DamageSource, и без поднятого сервера падает с NullPointerException.
 * Сам слушатель при этом работает на моках Plugin/PluginManager -
 * callEvent проверяется через verify, а не через диспетчер.
 */
class CrystalDamageListenerTest {

    private final Plugin plugin = mock(Plugin.class);
    private final Server server = mock(Server.class);
    private final PluginManager pluginManager = mock(PluginManager.class);
    private final EnderCrystal crystal = mock(EnderCrystal.class);
    private final Player victim = mock(Player.class);

    private StubConfig config;
    private CrystalDamageListener listener;

    @BeforeEach
    void setUp() {

        MockBukkit.mock();

        config = new StubConfig()
                .withMultiplier(4.0D)
                .withMessage(MessageKey.MESSAGE_BEFORE_DAMAGE, "boom");

        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(victim.isOnline()).thenReturn(true);

        listener = new CrystalDamageListener(plugin, config,
                new MultiplierService(config, new PermissionService(config)),
                new MessageService(config));

    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Удобный конструктор помечен на удаление, но именно он синтезирует
     * DamageSource через Bukkit.getUnsafe() - без поднятого сервера он
     * падает с NullPointerException, что и закрепляет @BeforeEach.
     */
    @SuppressWarnings("removal")
    private EntityDamageByEntityEvent hit(Entity damager, Entity damaged, double damage) {
        return new EntityDamageByEntityEvent(damager, damaged,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, damage);
    }

    @Test
    @DisplayName("Урон кристалла умножается, событие не отменяется")
    void crystalDamageIsMultiplied() {

        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(40.0D, event.getDamage());
        assertFalse(event.isCancelled());

    }

    @Test
    @DisplayName("Предупреждение уходит игроку до применения урона")
    void warningIsSentToTheVictim() {

        listener.onCrystalDamage(hit(crystal, victim, 10.0D));

        verify(victim).sendMessage("boom");

    }

    @Test
    @DisplayName("Нулевой множитель отменяет удар и не трогает урон")
    void zeroMultiplierCancelsTheHit() {

        config.withMultiplier(0.0D);
        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertTrue(event.isCancelled());
        assertEquals(10.0D, event.getDamage(), "урон не переписывается при отмене");

    }

    @Test
    @DisplayName("Право обхода оставляет ванильный урон")
    void bypassKeepsVanillaDamage() {

        config.withPermissionsEnabled(true);
        when(victim.hasPermission(config.getPermission(PermissionNode.BYPASS))).thenReturn(true);
        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(10.0D, event.getDamage());
        assertFalse(event.isCancelled());

    }

    @Test
    @DisplayName("Выключенный плагин не трогает событие и не пишет в чат")
    void disabledPluginLeavesEventAlone() {

        config.withPluginEnabled(false);
        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(10.0D, event.getDamage());
        assertFalse(event.isCancelled());
        verify(victim, never()).sendMessage(org.mockito.ArgumentMatchers.anyString());

    }

    @Test
    @DisplayName("Урон не от кристалла не пересчитывается")
    void otherDamagerIsIgnored() {

        EntityDamageByEntityEvent event = hit(mock(Zombie.class), victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(10.0D, event.getDamage());
        verify(pluginManager, never()).callEvent(any());

    }

    @Test
    @DisplayName("Урон не по игроку не пересчитывается")
    void nonPlayerVictimIsIgnored() {

        EntityDamageByEntityEvent event = hit(crystal, mock(Zombie.class), 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(10.0D, event.getDamage());
        verify(pluginManager, never()).callEvent(any());

    }

    @Test
    @DisplayName("Собственное событие несет жертву, кристалл и решение")
    void multiplyEventCarriesTheWholeDecision() {

        listener.onCrystalDamage(hit(crystal, victim, 10.0D));

        ArgumentCaptor<CrystalDamageMultiplyEvent> captor =
                ArgumentCaptor.forClass(CrystalDamageMultiplyEvent.class);

        verify(pluginManager).callEvent(captor.capture());

        CrystalDamageMultiplyEvent fired = captor.getValue();
        DamageDecision decision = fired.getDecision();

        assertSame(victim, fired.getVictim());
        assertSame(crystal, fired.getCrystal());
        assertEquals(4.0D, decision.multiplier());
        assertEquals(40.0D, decision.finalDamage());
        assertEquals(40.0D, fired.getDamage(), "стартовый урон события - из решения");

    }

    @Test
    @DisplayName("Отмена собственного события сохраняет ванильный урон")
    void cancelledMultiplyEventKeepsVanillaDamage() {

        doAnswer(invocation -> {
            invocation.getArgument(0, CrystalDamageMultiplyEvent.class).setCancelled(true);
            return null;
        }).when(pluginManager).callEvent(any(CrystalDamageMultiplyEvent.class));

        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(10.0D, event.getDamage());
        assertFalse(event.isCancelled());

    }

    @Test
    @DisplayName("Перехват урона в собственном событии применяется к удару")
    void damageOverrideIsApplied() {

        doAnswer(invocation -> {
            invocation.getArgument(0, CrystalDamageMultiplyEvent.class).setDamage(7.5D);
            return null;
        }).when(pluginManager).callEvent(any(CrystalDamageMultiplyEvent.class));

        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertEquals(7.5D, event.getDamage());
        assertFalse(event.isCancelled());

    }

    @Test
    @DisplayName("Отмена собственного события важнее отмены удара нулем")
    void cancelledMultiplyEventWinsOverZeroMultiplier() {

        config.withMultiplier(0.0D);

        doAnswer(invocation -> {
            invocation.getArgument(0, CrystalDamageMultiplyEvent.class).setCancelled(true);
            return null;
        }).when(pluginManager).callEvent(any(CrystalDamageMultiplyEvent.class));

        EntityDamageByEntityEvent event = hit(crystal, victim, 10.0D);

        listener.onCrystalDamage(event);

        assertFalse(event.isCancelled(), "сторона, отменившая событие, решает сама");
        assertEquals(10.0D, event.getDamage());

    }

}
