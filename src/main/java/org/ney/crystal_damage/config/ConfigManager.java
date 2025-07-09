package org.ney.crystal_damage.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.ney.crystal_damage.util.HexColorUtil;
import org.ney.crystal_damage.NeyUPCrystalDamage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Менеджер конфигурации плагина.
 * Реализует загрузку и парсинг настроек из config.yml.
 */
public class ConfigManager {

    private final NeyUPCrystalDamage plugin;
    private FileConfiguration config;

    public ConfigManager(@NotNull NeyUPCrystalDamage plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        loadConfig();
    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public double getDamageMultiplier() {
        return config.getDouble("settings.multiplier");
    }

    public boolean isExpEnabled() {
        return config.getBoolean("settings.drop-exp");
    }

    public boolean isMessageEnabled() {
        return config.getBoolean("messages.message-before-damage.enabled");
    }

    public List<String> getMessageLines() {
        return config.getStringList("messages.message-before-damage.text").stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }
}