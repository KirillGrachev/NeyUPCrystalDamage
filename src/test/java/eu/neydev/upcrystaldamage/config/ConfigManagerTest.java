package eu.neydev.upcrystaldamage.config;

import eu.neydev.upcrystaldamage.NeyUPCrystalDamage;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Проверка менеджера конфигурации на настоящем YAML-файле: дефолты,
 * чтение секций, устаревшие ключи 1.x, предупреждения и перезагрузка.
 *
 * Сервер не поднимается: плагин мокается, getDataFolder указывает на
 * временный каталог, а getConfig перечитывает файл на каждый вызов -
 * так reload() менеджера ведет себя как на живом ядре.
 */
class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private final List<LogRecord> consoleRecords = new ArrayList<>();

    private NeyUPCrystalDamage plugin() {

        NeyUPCrystalDamage plugin = mock(NeyUPCrystalDamage.class);

        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(countingLogger());
        // getConfig перечитывает файл на каждый вызов: reload() дергает
        // штатный reloadConfig (мок-noop) и забирает свежий конфиг
        when(plugin.getConfig()).thenAnswer(answer -> loadFromDisk());
        return plugin;

    }

    private ConfigManager manager(String yaml) {

        writeConfig(yaml);

        NeyUPCrystalDamage plugin = plugin();
        return new ConfigManager(plugin, new ConsoleService(plugin));

    }

    /**
     * Читает config.yml из tempDir без Bukkit-логгера: в тестах сервера нет,
     * а штатный loadConfiguration при битом YAML полез бы в Bukkit.getLogger().
     */
    private YamlConfiguration loadFromDisk() {

        YamlConfiguration loaded = new YamlConfiguration();

        try {
            loaded.load(tempDir.resolve("config.yml").toFile());
        } catch (Exception ignored) {
            // Битый или отсутствующий файл: пустая конфигурация, как в продакшене
        }

        return loaded;

    }

    private Logger countingLogger() {

        Logger logger = Logger.getLogger("config-test-" + System.nanoTime());

        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                consoleRecords.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }

        });

        return logger;

    }

    private void writeConfig(String yaml) {
        try {
            Files.writeString(tempDir.resolve("config.yml"), yaml);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось записать тестовый config.yml", exception);
        }
    }

    private boolean hasRecord(Level level, String token) {
        return consoleRecords.stream()
                .filter(record -> record.getLevel().equals(level))
                .anyMatch(record -> String.valueOf(record.getMessage()).contains(token));
    }

    @Test
    @DisplayName("Пустой файл дает нейтральные дефолты кода")
    void emptyFileFallsBackToCodeDefaults() {

        ConfigManager manager = manager("");

        assertTrue(manager.isPluginEnabled());
        assertEquals(1.0D, manager.getDamageMultiplier(), "нейтральный множитель");
        assertFalse(manager.arePermissionsEnabled());
        assertTrue(manager.areMessagesEnabled());
        assertEquals("", manager.getMessagePrefix());
        assertEquals("crystal.damage.bypass", manager.getPermission(PermissionNode.BYPASS));
        assertEquals("crystaldamage.reload", manager.getPermission(PermissionNode.RELOAD));
        assertFalse(hasRecord(Level.SEVERE, "broken"), "пустой файл - не поломка");

    }

    @Test
    @DisplayName("Отсутствующий файл не считается битым")
    void missingFileIsNotBroken() {

        NeyUPCrystalDamage plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));

        assertEquals(1.0D, manager.getDamageMultiplier());
        assertFalse(hasRecord(Level.SEVERE, "broken"));

    }

    @Test
    @DisplayName("Значения секций читаются из файла")
    void readsEverySection() {

        ConfigManager manager = manager("""
                settings:
                  enabled: false
                  multiplier: 4.0
                permissions:
                  enabled: true
                  bypass: "donor.crystal"
                messages:
                  enabled: false
                  prefix: "&#ff0000CD &8» &7"
                """);

        assertFalse(manager.isPluginEnabled());
        assertEquals(4.0D, manager.getDamageMultiplier());
        assertTrue(manager.arePermissionsEnabled());
        assertEquals("donor.crystal", manager.getPermission(PermissionNode.BYPASS));
        assertFalse(manager.areMessagesEnabled());
        assertTrue(manager.getMessagePrefix().contains("§x"), "префикс окрашен: "
                + manager.getMessagePrefix());

    }

    @Test
    @DisplayName("Отрицательный множитель прижимается к нулю с предупреждением")
    void negativeMultiplierIsClampedWithWarning() {

        ConfigManager manager = manager("settings:\n  multiplier: -2.5\n");

        assertEquals(0.0D, manager.getDamageMultiplier());
        assertTrue(hasRecord(Level.WARNING, "settings.multiplier"),
                "в консоли назван проблемный путь");
        assertTrue(hasRecord(Level.WARNING, "-2.5"), "в консоли показано значение");

    }

    @Test
    @DisplayName("NaN в множителе тоже прижимается к нулю")
    void notANumberMultiplierIsClamped() {

        ConfigManager manager = manager("settings:\n  multiplier: .nan\n");

        assertEquals(0.0D, manager.getDamageMultiplier());
        assertTrue(hasRecord(Level.WARNING, "settings.multiplier"));

    }

    @Test
    @DisplayName("Ноль разрешен: он отменяет удар, а не ломает конфигурацию")
    void zeroMultiplierIsAccepted() {

        ConfigManager manager = manager("settings:\n  multiplier: 0\n");

        assertEquals(0.0D, manager.getDamageMultiplier());
        assertFalse(hasRecord(Level.WARNING, "settings.multiplier"));

    }

    @Test
    @DisplayName("Устаревший ключ права 1.x читается с подсказкой в консоль")
    void legacyPermissionPathIsRead() {

        ConfigManager manager = manager("""
                permissions:
                  enabled: true
                  bypass-permission: "legacy.bypass"
                """);

        assertEquals("legacy.bypass", manager.getPermission(PermissionNode.BYPASS));
        assertTrue(hasRecord(Level.WARNING, "permissions.bypass-permission"));
        assertTrue(hasRecord(Level.WARNING, "permissions.bypass"));

    }

    @Test
    @DisplayName("Новый ключ права побеждает устаревший без предупреждения")
    void newPermissionPathWinsOverLegacy() {

        ConfigManager manager = manager("""
                permissions:
                  bypass: "new.node"
                  bypass-permission: "legacy.node"
                """);

        assertEquals("new.node", manager.getPermission(PermissionNode.BYPASS));
        assertFalse(hasRecord(Level.WARNING, "bypass-permission"));

    }

    @Test
    @DisplayName("Устаревшая секция сообщения 1.x читается")
    void legacyMessageSectionIsRead() {

        ConfigManager manager = manager("""
                messages:
                  message-before-damage:
                    enabled: true
                    text:
                      - "legacy line"
                """);

        assertEquals(List.of("legacy line"), manager.getMessages(MessageKey.MESSAGE_BEFORE_DAMAGE));
        assertTrue(hasRecord(Level.WARNING, "messages.message-before-damage"));

    }

    @Test
    @DisplayName("Выключенное сообщение дает пустой список строк")
    void disabledMessageBecomesEmptyList() {

        ConfigManager manager = manager("""
                messages:
                  message_before_damage:
                    enabled: false
                    text:
                      - "не должно читаться"
                """);

        assertEquals(List.of(), manager.getMessages(MessageKey.MESSAGE_BEFORE_DAMAGE));

    }

    @Test
    @DisplayName("Текст одной строкой становится списком из одной строки")
    void scalarTextBecomesSingleLine() {

        ConfigManager manager = manager("messages:\n  reload:\n    text: \"single line\"\n");

        assertEquals(List.of("single line"), manager.getMessages(MessageKey.RELOAD));

    }

    @Test
    @DisplayName("Пустые элементы списка пропускаются, пустые строки остаются")
    void nullListElementsAreSkipped() {

        ConfigManager manager = manager("""
                messages:
                  usage:
                    text:
                      - "first"
                      -
                      - ""
                      - "last"
                """);

        assertEquals(List.of("first", "", "last"), manager.getMessages(MessageKey.USAGE));

    }

    @Test
    @DisplayName("Дефолт сообщения многострочный и совпадает с config.yml")
    void defaultMessageKeepsTrailingBlankLine() {

        List<String> lines = manager("").getMessages(MessageKey.MESSAGE_BEFORE_DAMAGE);

        assertEquals(3, lines.size(), "пустая строка сверху, текст, пустая строка снизу");
        assertEquals("", lines.get(0));
        assertEquals("", lines.get(2));
        assertTrue(lines.get(1).contains("Boom!"), "текст на месте: " + lines.get(1));

    }

    @Test
    @DisplayName("Многострочный дефолт usage режется по строкам")
    void multiLineDefaultIsSplit() {

        List<String> lines = manager("").getMessages(MessageKey.USAGE);

        assertEquals(3, lines.size());
        assertTrue(lines.get(0).contains("NeyUPCrystalDamage commands:"));
        assertTrue(lines.get(1).contains("/crystaldamage reload"));

    }

    @Test
    @DisplayName("Битый YAML кричит в SEVERE и работает на дефолтах")
    void brokenYamlLogsSevere() {

        ConfigManager manager = manager("settings: [\n  multiplier: 4.0\n");

        assertEquals(1.0D, manager.getDamageMultiplier(), "подставлен дефолт");
        assertTrue(hasRecord(Level.SEVERE, "config.yml is broken"));

    }

    @Test
    @DisplayName("reload перечитывает файл и уведомляет подписчиков")
    void reloadRereadsFileAndNotifies() {

        writeConfig("settings:\n  multiplier: 2.0\n");

        NeyUPCrystalDamage plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));
        AtomicInteger notifications = new AtomicInteger();
        manager.onReload(notifications::incrementAndGet);

        assertEquals(2.0D, manager.getDamageMultiplier());

        writeConfig("settings:\n  multiplier: 8.0\n");
        manager.reload();

        assertEquals(8.0D, manager.getDamageMultiplier());
        assertEquals(1, notifications.get());

    }

    @Test
    @DisplayName("Снапшот подменяется целиком:reload не рвет согласованность")
    void reloadSwapsWholeSnapshot() {

        writeConfig("""
                settings:
                  multiplier: 3.0
                permissions:
                  enabled: false
                """);

        NeyUPCrystalDamage plugin = plugin();
        ConfigManager manager = new ConfigManager(plugin, new ConsoleService(plugin));

        writeConfig("""
                settings:
                  multiplier: 5.0
                permissions:
                  enabled: true
                """);
        manager.reload();

        assertEquals(5.0D, manager.getDamageMultiplier());
        assertTrue(manager.arePermissionsEnabled());

    }

}
