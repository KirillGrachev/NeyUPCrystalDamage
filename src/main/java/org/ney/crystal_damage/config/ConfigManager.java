package org.ney.crystal_damage.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.ney.crystal_damage.NeyUPCrystalDamage;
import org.ney.crystal_damage.util.HexColorUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final NeyUPCrystalDamage plugin;
    private FileConfiguration config;

    private static final String PATH_MULTIPLIER = "settings.multiplier";
    private static final String PATH_DROP_EXP = "settings.drop-exp";
    private static final String PATH_MESSAGE_ENABLED = "messages.message-before-damage.enabled";
    private static final String PATH_MESSAGE_TEXT = "messages.message-before-damage.text";

    private double damageMultiplier;
    private boolean expEnabled;
    private boolean messageEnabled;
    private List<String> messageLines;

    public ConfigManager(@NotNull NeyUPCrystalDamage plugin) {

        this.plugin = plugin;
        saveDefaultConfig();

        loadConfig();
        cacheConfigValues();

    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void cacheConfigValues() {
        damageMultiplier = config.getDouble(PATH_MULTIPLIER, 1.0);
        expEnabled = config.getBoolean(PATH_DROP_EXP, true);
        messageEnabled = config.getBoolean(PATH_MESSAGE_ENABLED, true);
        messageLines = getStringListWithColor(PATH_MESSAGE_TEXT);
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public boolean isExpEnabled() {
        return expEnabled;
    }

    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    public List<String> getMessageLines() {
        return messageLines;
    }

    private List<String> getStringListWithColor(String path) {
        return config.getStringList(path).stream()
                .map(HexColorUtil::color)
                .collect(Collectors.toList());
    }

    public void reload() {
        loadConfig();
        cacheConfigValues();
    }
}