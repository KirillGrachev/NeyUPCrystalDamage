package eu.neydev.upcrystaldamage.config;

import eu.neydev.upcrystaldamage.config.type.MessageKey;
import eu.neydev.upcrystaldamage.config.type.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Контракт конфигурации NeyUPCrystalDamage.
 * Все остальные компоненты зависят от интерфейса, а не от реализации:
 * сервисы и слушатель не знают, откуда приехали значения (YAML, дефолты
 * кода или подставленный в тесте стаб).
 */
public interface CrystalDamageConfig {

    /**
     * Мастер-выключатель плагина: при false урон не трогается вовсе.
     */
    boolean isPluginEnabled();

    /**
     * Множитель урона кристалла. Ноль означает отмену удара,
     * отрицательные значения до чтения отсекаются менеджером конфигурации.
     */
    double getDamageMultiplier();

    boolean arePermissionsEnabled();

    /**
     * Строка узла права из конфигурации (или дефолт узла).
     *
     * @param node узел права
     * @return название права для Bukkit
     */
    @NotNull String getPermission(@NotNull PermissionNode node);

    boolean areMessagesEnabled();

    /**
     * Префикс, подставляемый в плейсхолдер {prefix}. Уже окрашен.
     */
    @NotNull String getMessagePrefix();

    /**
     * Строки сообщения по ключу.
     *
     * @param key ключ сообщения
     * @return окрашенные строки; пустой список, если сообщение выключено
     */
    @NotNull List<String> getMessages(@NotNull MessageKey key);

}
