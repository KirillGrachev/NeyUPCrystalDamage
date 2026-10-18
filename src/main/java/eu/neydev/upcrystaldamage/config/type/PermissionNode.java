package eu.neydev.upcrystaldamage.config.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Узлы прав плагина.
 *
 * Значения читаются из конфигурации, поэтому их можно переопределить.
 * {@code legacyConfigKey} - путь версии 1.x ({@code bypass-permission}),
 * он читается с предупреждением в консоль, если нового ключа еще нет.
 */
public enum PermissionNode {

    BYPASS("bypass", "bypass-permission", "crystal.damage.bypass"),
    RELOAD("reload", null, "crystaldamage.reload");

    private final String configKey;
    private final @Nullable String legacyConfigKey;
    private final String defaultPermission;

    PermissionNode(String configKey, @Nullable String legacyConfigKey, String defaultPermission) {
        this.configKey = configKey;
        this.legacyConfigKey = legacyConfigKey;
        this.defaultPermission = defaultPermission;
    }

    public @NotNull String getConfigKey() {
        return configKey;
    }

    /**
     * Путь ключа в конфигурации 1.x или null, если узел появился в 2.0.0.
     */
    public @Nullable String getLegacyConfigKey() {
        return legacyConfigKey;
    }

    public @NotNull String getDefaultPermission() {
        return defaultPermission;
    }

}
