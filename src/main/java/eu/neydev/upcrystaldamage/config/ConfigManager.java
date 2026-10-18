package eu.neydev.upcrystaldamage.config;

import eu.neydev.upcrystaldamage.NeyUPCrystalDamage;
import eu.neydev.upcrystaldamage.config.type.ConsoleMessage;
import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import eu.neydev.upcrystaldamage.service.ConsoleService;
import eu.neydev.upcrystaldamage.util.HexColorUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * Менеджер конфигурации NeyUPCrystalDamage.
 *
 * Значения читаются один раз и собираются в неизменяемый снапшот, который
 * подменяется одной volatile-записью. Слушатель урона поэтому всегда видит
 * согласованный набор («флаг новый, множитель старый» невозможно), а reload
 * не блокирует главный поток: чтения из обработчика событий не трогают ни
 * диск, ни YAML. Некорректные значения не роняют загрузку - подставляется
 * граница, а в консоль уходит предупреждение через {@link ConsoleService}.
 *
 * Конфигурации 1.x читаются без ручной миграции: переименованные ключи
 * (permissions.bypass-permission, messages.message-before-damage) находятся
 * по старым путям, о чем в консоль уходит единственная подсказка LEGACY_PATH.
 * Файл на диске не переписывается - решение о правке остается за владельцем.
 *
 * Источник истины один - {@code plugin.getConfig()}: отдельного парсинга файла
 * менеджер не ведет, reload использует штатный reloadConfig(). Поврежденный
 * YAML не тонет в тишине: Bukkit глотает InvalidConfigurationException и отдает
 * пустую конфигурацию, поэтому файл перечитывается явно и ошибка уходит в
 * SEVERE-лог со стектрейсом.
 */
public class ConfigManager implements CrystalDamageConfig {

    private final NeyUPCrystalDamage plugin;
    private final ConsoleService consoleService;
    private final List<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

    private static final String PATH_ENABLED = "settings.enabled";
    private static final String PATH_MULTIPLIER = "settings.multiplier";
    private static final String PATH_PERMISSIONS_ENABLED = "permissions.enabled";
    private static final String PATH_MESSAGES_ENABLED = "messages.enabled";
    private static final String PATH_MESSAGE_PREFIX = "messages.prefix";

    /**
     * Множитель по умолчанию нейтрален: отсутствие ключа в конфигурации
     * не должно ни усиливать, ни гасить урон.
     */
    private static final double DEFAULT_MULTIPLIER = 1.0D;

    /** Нижняя граница множителя: ноль разрешен (отменяет удар), отрицательный урон - нет. */
    private static final double MIN_MULTIPLIER = 0.0D;

    /** Окрашенные дефолты: fallback {@link #getMessages} не пересобирает строки на каждый вызов. */
    private static final Map<MessageKey, List<String>> DEFAULT_MESSAGES;

    static {

        Map<MessageKey, List<String>> defaults = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {
            defaults.put(key, coloredLines(key.getDefaultMessage()));
        }

        DEFAULT_MESSAGES = Collections.unmodifiableMap(defaults);

    }

    private record SettingsSection(boolean enabled, double multiplier) {
    }

    private record PermissionsSection(boolean enabled, Map<PermissionNode, String> permissions) {
    }

    private record MessagingSection(boolean enabled,
                                    String prefix,
                                    Map<MessageKey, List<String>> messages) {
    }

    private record ConfigSnapshot(SettingsSection settings,
                                  PermissionsSection permissions,
                                  MessagingSection messaging) {
    }

    /**
     * Текущий снапшот. Единственное изменяемое поле менеджера; подменяется
     * атомарно, поэтому составные значения никогда не читаются «наполовину».
     */
    private volatile ConfigSnapshot snapshot;

    private FileConfiguration config;

    public ConfigManager(@NotNull NeyUPCrystalDamage plugin, @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.consoleService = consoleService;

        plugin.saveDefaultConfig();

        this.config = plugin.getConfig();

        validateReadable();
        this.snapshot = buildSnapshot();

    }

    /**
     * Перезагружает конфигурацию и уведомляет подписанные компоненты.
     */
    public void reload() {

        plugin.reloadConfig();

        this.config = plugin.getConfig();

        validateReadable();

        this.snapshot = buildSnapshot();

        reloadListeners.forEach(Runnable::run);

    }

    /**
     * Подписывает компонент на перезагрузку конфигурации.
     *
     * @param listener действие, выполняемое после reload
     */
    public void onReload(@NotNull Runnable listener) {
        reloadListeners.add(listener);
    }

    @Override
    public boolean isPluginEnabled() {
        return snapshot.settings().enabled();
    }

    @Override
    public double getDamageMultiplier() {
        return snapshot.settings().multiplier();
    }

    @Override
    public boolean arePermissionsEnabled() {
        return snapshot.permissions().enabled();
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return snapshot.permissions().permissions().getOrDefault(node, node.getDefaultPermission());
    }

    @Override
    public boolean areMessagesEnabled() {
        return snapshot.messaging().enabled();
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return snapshot.messaging().prefix();
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return snapshot.messaging().messages()
                .getOrDefault(key, DEFAULT_MESSAGES.getOrDefault(key, List.of()));
    }

    /**
     * Явно перечитывает файл, если результат штатной загрузки пуст:
     * поврежденный YAML должен кричать в консоль, а не тихо работать на дефолтах.
     */
    private void validateReadable() {

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists() || !config.getKeys(false).isEmpty()) {
            return;
        }

        try {
            new YamlConfiguration().load(configFile);
        } catch (IOException | InvalidConfigurationException exception) {
            plugin.getLogger().log(Level.SEVERE,
                    "config.yml is broken and cannot be parsed - built-in defaults are used. "
                            + "Fix the file and run /crystaldamage reload.",
                    exception);
        }

    }

    private @NotNull ConfigSnapshot buildSnapshot() {
        return new ConfigSnapshot(buildSettingsSection(),
                buildPermissionsSection(),
                buildMessagingSection());
    }

    private @NotNull SettingsSection buildSettingsSection() {
        return new SettingsSection(
                config.getBoolean(PATH_ENABLED, true),
                doubleOrWarn(config.getDouble(PATH_MULTIPLIER, DEFAULT_MULTIPLIER),
                        PATH_MULTIPLIER, MIN_MULTIPLIER)
        );
    }

    private @NotNull PermissionsSection buildPermissionsSection() {

        Map<PermissionNode, String> permissions = new EnumMap<>(PermissionNode.class);

        for (PermissionNode node : PermissionNode.values()) {

            String path = keyedPath("permissions.", node.getConfigKey(), node.getLegacyConfigKey());
            permissions.put(node, config.getString(path, node.getDefaultPermission()));

        }

        return new PermissionsSection(
                config.getBoolean(PATH_PERMISSIONS_ENABLED, false),
                Collections.unmodifiableMap(permissions)
        );

    }

    private @NotNull MessagingSection buildMessagingSection() {

        Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);

        for (MessageKey key : MessageKey.values()) {

            String path = keyedPath("messages.", key.getConfigKey(), key.getLegacyConfigKey());

            if (!config.getBoolean(path + ".enabled", key.isDefaultEnabled())) {
                messages.put(key, List.of());
                continue;
            }

            messages.put(key, readColoredLines(path + ".text", key.getDefaultMessage()));

        }

        return new MessagingSection(
                config.getBoolean(PATH_MESSAGES_ENABLED, true),
                color(config.getString(PATH_MESSAGE_PREFIX, "")),
                Collections.unmodifiableMap(messages)
        );

    }

    /**
     * Возвращает путь ключа с откатом на устаревший.
     *
     * Новый путь побеждает всегда: если серверовладелец продублировал ключ,
     * читается актуальный, а предупреждение не печатается. Если в файле нет
     * ни того ни другого, возвращается новый путь - значение прочитается
     * как отсутствующее и подставится дефолт.
     *
     * @param prefix    префикс секции ({@code messages.}, {@code permissions.})
     * @param key       текущий ключ
     * @param legacyKey ключ версии 1.x или null, если ключ появился в 2.0.0
     * @return путь для чтения значения
     */
    private @NotNull String keyedPath(@NotNull String prefix,
                                      @NotNull String key,
                                      @Nullable String legacyKey) {

        String path = prefix + key;

        if (config.isSet(path) || legacyKey == null) {
            return path;
        }

        String legacyPath = prefix + legacyKey;

        if (!config.isSet(legacyPath)) {
            return path;
        }

        consoleService.log(ConsoleMessage.LEGACY_PATH,
                "path", legacyPath,
                "replacement", path);
        return legacyPath;

    }

    private @NotNull List<String> readColoredLines(@NotNull String path, @NotNull String defaultValue) {

        Object raw = config.get(path);

        if (raw == null) {
            return coloredLines(defaultValue);
        }

        if (raw instanceof List<?> list) {

            List<String> lines = new ArrayList<>(list.size());

            for (Object element : list) {
                if (element != null) {
                    lines.add(color(String.valueOf(element)));
                }
            }

            return Collections.unmodifiableList(lines);

        }

        return coloredLines(String.valueOf(raw));

    }

    private double doubleOrWarn(double value, @NotNull String path, double minimum) {

        if (value >= minimum && !Double.isNaN(value)) {
            return value;
        }

        consoleService.log(ConsoleMessage.INVALID_VALUE,
                "path", path,
                "value", String.valueOf(value),
                "defaultValue", String.valueOf(minimum));
        return minimum;

    }

    /**
     * Окрашивает текст и режет его на строки.
     *
     * Многострочные дефолты (usage, info, message_before_damage) живут одним
     * значением с \n, а отправляются списком: так один и тот же текст
     * одинаково читается и в коде, и в config.yml, где он задан списком строк.
     * Лимит -1 у split обязателен - без него хвостовые пустые строки
     * теряются, и дефолт сообщения расходится с тем, что лежит в файле.
     */
    private static @NotNull List<String> coloredLines(@NotNull String text) {
        return List.of(HexColorUtil.color(text).split("\n", -1));
    }

    private @NotNull String color(@Nullable String text) {
        return HexColorUtil.color(text);
    }

}
