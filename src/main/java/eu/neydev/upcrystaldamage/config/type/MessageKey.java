package eu.neydev.upcrystaldamage.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ключи сообщений плагина.
 *
 * Каждый ключ хранит путь в config.yml, дефолтный текст на английском
 * и признак включенности по умолчанию. {@code legacyConfigKey} - путь
 * версии 1.x: конфигурации старше 2.0.0 продолжают читаться, а в консоль
 * уходит подсказка о переименовании (файл на диске не переписывается).
 */
public enum MessageKey {

    STATE_ON("state_on", null, true, "&aenabled"),

    STATE_OFF("state_off", null, true, "&cdisabled"),

    MESSAGE_BEFORE_DAMAGE("message_before_damage", "message-before-damage", true,
            "\n #5dff52[⛄] &fBoom! Be more careful next time! \n"),

    NO_PERMISSION("no_permission", null, true,
            "{prefix}&#ff6b6bYou do not have permission to do this."),

    USAGE("usage", null, true, "{prefix}&#8ce0ffNeyUPCrystalDamage commands:\n"
            + "&f/crystaldamage reload &8- &7reload the configuration\n"
            + "&f/crystaldamage info &8- &7current multiplier and bypass state"),

    RELOAD("reload", null, true,
            "{prefix}&#9dff8cConfiguration reloaded &7({time} ms)."),

    INFO("info", null, true, "{prefix}&7Version: &f{version}\n"
            + "&7Multiplier: &f{multiplier}\n"
            + "&7Bypass: &f{bypass_state} &8({bypass})");

    private final String configKey;
    private final @Nullable String legacyConfigKey;
    private final boolean defaultEnabled;
    private final String defaultMessage;

    MessageKey(String configKey, @Nullable String legacyConfigKey,
               boolean defaultEnabled, String defaultMessage) {
        this.configKey = configKey;
        this.legacyConfigKey = legacyConfigKey;
        this.defaultEnabled = defaultEnabled;
        this.defaultMessage = defaultMessage;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    /**
     * Путь секции в конфигурации 1.x или null, если ключ появился в 2.0.0.
     */
    public @Nullable String getLegacyConfigKey() {
        return legacyConfigKey;
    }

    /**
     * Включено ли сообщение, если его секции нет в конфигурации.
     */
    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public @NotNull String getDefaultMessage() {
        return defaultMessage;
    }

}
