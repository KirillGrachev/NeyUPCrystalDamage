package eu.neydev.upcrystaldamage.support;

import eu.neydev.upcrystaldamage.config.CrystalDamageConfig;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Изменяемый стаб конфигурации для юнит-тестов.
 *
 * Сервисы проверяются без Bukkit и без YAML: тест задает ровно те значения,
 * которые нужны сценарию, и не зависит от порядка ключей в config.yml.
 * Дефолты повторяют поставляемый файл, поэтому «в тесте ничего не настроили»
 * означает то же, что на свежем сервере.
 */
public final class StubConfig implements CrystalDamageConfig {

    private boolean pluginEnabled = true;
    private double multiplier = 0.1D;
    private boolean permissionsEnabled = false;
    private boolean messagesEnabled = true;
    private String prefix = "";
    private final Map<PermissionNode, String> permissions = new EnumMap<>(PermissionNode.class);
    private final Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);

    public @NotNull StubConfig withPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withMultiplier(double value) {
        this.multiplier = value;
        return this;
    }

    public @NotNull StubConfig withPermissionsEnabled(boolean enabled) {
        this.permissionsEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withMessagesEnabled(boolean enabled) {
        this.messagesEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withPrefix(@NotNull String value) {
        this.prefix = value;
        return this;
    }

    public @NotNull StubConfig withPermission(@NotNull PermissionNode node, @NotNull String value) {
        permissions.put(node, value);
        return this;
    }

    public @NotNull StubConfig withMessage(@NotNull MessageKey key, @NotNull String... lines) {
        messages.put(key, Arrays.asList(lines));
        return this;
    }

    @Override
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    @Override
    public double getDamageMultiplier() {
        return multiplier;
    }

    @Override
    public boolean arePermissionsEnabled() {
        return permissionsEnabled;
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return permissions.getOrDefault(node, node.getDefaultPermission());
    }

    @Override
    public boolean areMessagesEnabled() {
        return messagesEnabled;
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return prefix;
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return messages.getOrDefault(key, List.of());
    }

}
