package eu.neydev.upcrystaldamage;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Интеграционные тесты на MockBukkit: плагин загружается в живой серверный
 * мок, слушатель реагирует через настоящий диспетчер событий, команды
 * исполняются через command map, конфигурация сохраняется в папку плагина.
 */
class NeyUPCrystalDamageIntegrationTest {

    private ServerMock server;
    private NeyUPCrystalDamage plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(NeyUPCrystalDamage.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Удобный конструктор помечен на удаление, но именно он синтезирует
     * DamageSource через Bukkit.getUnsafe() - то самое окруженческое
     * допущение, которое набор тестов фиксирует.
     */
    @SuppressWarnings("removal")
    private EntityDamageByEntityEvent crystalHit(PlayerMock victim, double damage) {
        return new EntityDamageByEntityEvent(mock(EnderCrystal.class), victim,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, damage);
    }

    @Test
    @DisplayName("Плагин стартует: сервисы, команда и обработчики на месте")
    void pluginStartsAndWiresEverything() {

        assertTrue(plugin.isEnabled());
        assertNotNull(plugin.getServices());
        assertNotNull(plugin.getConfigManager());
        assertNotNull(plugin.getConsoleService());
        assertNotNull(plugin.getServices().getMultiplierService());

        PluginCommand command = plugin.getCommand("crystaldamage");

        assertNotNull(command, "команда из plugin.yml найдена");
        assertNotNull(command.getExecutor(), "executor привязан");
        assertNotNull(command.getTabCompleter(), "tab-completer привязан");

    }

    @Test
    @DisplayName("Права из plugin.yml зарегистрированы на сервере")
    void permissionsFromPluginYmlAreRegistered() {

        assertNotNull(server.getPluginManager().getPermission("crystal.damage.bypass"));
        assertNotNull(server.getPluginManager().getPermission("crystaldamage.reload"));
        assertNotNull(server.getPluginManager().getPermission("crystaldamage.admin"));

    }

    @Test
    @DisplayName("Поставляемый config.yml читается в документированные значения")
    void shippedConfigCarriesDocumentedDefaults() {

        assertTrue(plugin.getDataFolder().toPath().resolve("config.yml").toFile().exists(),
                "config.yml сохранен в папку плагина");
        assertEquals(0.1D, plugin.getConfigManager().getDamageMultiplier());
        assertFalse(plugin.getConfigManager().arePermissionsEnabled());
        assertEquals(3, plugin.getConfigManager().getMessages(MessageKey.MESSAGE_BEFORE_DAMAGE).size());

    }

    @Test
    @DisplayName("Удар кристалла проходит через настоящий диспетчер событий")
    void crystalHitIsMultipliedThroughTheDispatcher() {

        PlayerMock player = server.addPlayer();
        EntityDamageByEntityEvent event = crystalHit(player, 10.0D);

        server.getPluginManager().callEvent(event);

        assertEquals(1.0D, event.getDamage(), 1.0E-9, "множитель 0.1 из config.yml");
        assertFalse(event.isCancelled());

    }

    @Test
    @DisplayName("Игрок получает предупреждение до урона")
    void victimReceivesTheWarning() {

        PlayerMock player = server.addPlayer();

        server.getPluginManager().callEvent(crystalHit(player, 10.0D));

        String message = player.nextMessage();

        assertEquals("", message, "первая строка сообщения пустая");
        assertTrue(String.valueOf(player.nextMessage()).contains("Boom!"));

    }

    @Test
    @DisplayName("Уже отмененный удар не пересчитывается")
    void cancelledEventIsNotProcessed() {

        PlayerMock player = server.addPlayer();
        EntityDamageByEntityEvent event = crystalHit(player, 10.0D);

        event.setCancelled(true);
        server.getPluginManager().callEvent(event);

        assertEquals(10.0D, event.getDamage(), 1.0E-9, "ignoreCancelled оставляет урон ванильным");

    }

    @Test
    @DisplayName("Урон не по игроку остается ванильным")
    @SuppressWarnings("removal")
    void nonPlayerVictimIsIgnored() {

        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(mock(EnderCrystal.class),
                mock(org.bukkit.entity.Zombie.class),
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 10.0D);

        server.getPluginManager().callEvent(event);

        assertEquals(10.0D, event.getDamage(), 1.0E-9);

    }

    @Test
    @DisplayName("Команда reload исполняется через command map")
    void reloadCommandRunsThroughCommandMap() {

        PlayerMock player = server.addPlayer();

        assertTrue(player.performCommand("crystaldamage reload"));
        assertTrue(String.valueOf(player.nextMessage()).contains("Configuration reloaded"));

    }

    @Test
    @DisplayName("Команда info печатает множитель из снапшота")
    void infoCommandReportsMultiplier() {

        PlayerMock player = server.addPlayer();

        player.performCommand("crystaldamage info");

        // info задан в config.yml одной строкой с \n и уходит тремя сообщениями
        String version = String.valueOf(player.nextMessage());
        String multiplier = String.valueOf(player.nextMessage());
        String bypass = String.valueOf(player.nextMessage());

        assertTrue(version.contains("Version") && version.contains("2.0.0"), version);
        assertTrue(multiplier.contains("Multiplier") && multiplier.contains("0.1"), multiplier);
        assertTrue(bypass.contains("Bypass") && bypass.contains("crystal.damage.bypass"), bypass);

    }

    @Test
    @DisplayName("Без аргументов команда печатает справку")
    void commandWithoutArgumentsPrintsUsage() {

        PlayerMock player = server.addPlayer();

        player.performCommand("crystaldamage");

        assertTrue(String.valueOf(player.nextMessage()).contains("NeyUPCrystalDamage commands:"));

    }

    @Test
    @DisplayName("Автодополнение команды работает через сервер")
    void tabCompletionIsAvailable() {

        PlayerMock player = server.addPlayer();
        PluginCommand command = plugin.getCommand("crystaldamage");

        assertNotNull(command);
        assertEquals(java.util.List.of("reload"), command.getTabCompleter().onTabComplete(player, command,
                "crystaldamage", new String[]{"re"}));

    }

    @Test
    @DisplayName("Отключение плагина проходит без ошибок и снимает команду")
    void pluginStopsGracefully() {

        server.getPluginManager().disablePlugin(plugin);

        assertFalse(plugin.isEnabled());

    }

}
