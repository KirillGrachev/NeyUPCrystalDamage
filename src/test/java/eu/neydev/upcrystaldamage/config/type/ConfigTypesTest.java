package eu.neydev.upcrystaldamage.config.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Контракты перечислений конфигурации: ключи уникальны, дефолты непустые,
 * устаревшие пути помечены только там, где ключ переименован в 2.0.0.
 */
class ConfigTypesTest {

    @Test
    @DisplayName("Ключи сообщений уникальны и совпадают с config.yml")
    void messageKeysAreUnique() {

        Set<String> keys = new HashSet<>();

        for (MessageKey key : MessageKey.values()) {

            assertTrue(keys.add(key.getConfigKey()), "дубль ключа " + key.getConfigKey());
            assertFalse(key.getConfigKey().isBlank(), "пустой ключ у " + key);
            assertNotNull(key.getDefaultMessage(), "нет дефолта у " + key);
            assertFalse(key.getDefaultMessage().isBlank(), "пустой дефолт у " + key);

        }

        assertEquals(MessageKey.values().length, keys.size());

    }

    @Test
    @DisplayName("Устаревший путь помечен только у message_before_damage")
    void onlyRenamedMessageCarriesLegacyKey() {

        assertEquals("message-before-damage", MessageKey.MESSAGE_BEFORE_DAMAGE.getLegacyConfigKey());

        for (MessageKey key : MessageKey.values()) {
            if (key != MessageKey.MESSAGE_BEFORE_DAMAGE) {
                assertNull(key.getLegacyConfigKey(), "неожиданный legacy-ключ у " + key);
            }
        }

    }

    @Test
    @DisplayName("Все сообщения по умолчанию включены")
    void allMessagesAreEnabledByDefault() {
        for (MessageKey key : MessageKey.values()) {
            assertTrue(key.isDefaultEnabled(), "сообщение выключено по умолчанию: " + key);
        }
    }

    @Test
    @DisplayName("Узлы прав уникальны, legacy-ключ только у bypass")
    void permissionNodesAreUnique() {

        Set<String> keys = new HashSet<>();

        for (PermissionNode node : PermissionNode.values()) {

            assertTrue(keys.add(node.getConfigKey()), "дубль ключа " + node.getConfigKey());
            assertFalse(node.getDefaultPermission().isBlank(), "пустое право у " + node);

        }

        assertEquals("bypass-permission", PermissionNode.BYPASS.getLegacyConfigKey());
        assertNull(PermissionNode.RELOAD.getLegacyConfigKey());
        assertEquals("crystal.damage.bypass", PermissionNode.BYPASS.getDefaultPermission());
        assertEquals("crystaldamage.reload", PermissionNode.RELOAD.getDefaultPermission());

    }

    @Test
    @DisplayName("Шаблоны консоли содержат свои плейсхолдеры")
    void consoleTemplatesCarryPlaceholders() {

        for (ConsoleMessage message : ConsoleMessage.values()) {

            String template = message.getDefaultTemplate();

            assertFalse(template.isBlank(), "пустой шаблон у " + message);
            assertTrue(template.contains("{"), "нет плейсхолдеров у " + message);

        }

        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{path}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{value}"));
        assertTrue(ConsoleMessage.INVALID_VALUE.getDefaultTemplate().contains("{defaultValue}"));
        assertTrue(ConsoleMessage.LEGACY_PATH.getDefaultTemplate().contains("{replacement}"));
        assertTrue(ConsoleMessage.COMMAND_MISSING.getDefaultTemplate().contains("{command}"));
        assertTrue(ConsoleMessage.COMMAND_UNREGISTER_FAILED.getDefaultTemplate().contains("{reason}"));

    }

}
