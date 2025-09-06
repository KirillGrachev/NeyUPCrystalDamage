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
    private static final String PATH_MESSAGE_ENABLED = "messages.message-before-damage.enabled";
    private static final String PATH_MESSAGE_TEXT = "messages.message-before-damage.text";
    private static final String PATH_PERMISSIONS_ENABLED = "permissions.enabled";
    private static final String PATH_BYPASS_PERMISSION = "permissions.bypass-permission";

    private double damageMultiplier;
    private boolean messageEnabled;
    private List<String> messageLines;
    private boolean bypassPermissionEnabled;
    private String bypassPermission;

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
        messageEnabled = config.getBoolean(PATH_MESSAGE_ENABLED, true);
        messageLines = getStringListWithColor(PATH_MESSAGE_TEXT);
        bypassPermissionEnabled = config.getBoolean(PATH_PERMISSIONS_ENABLED, true);
        bypassPermission = config.getString(PATH_BYPASS_PERMISSION, "crystal.damage.bypass");
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
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

    public boolean isBypassPermissionEnabled() {
        return bypassPermissionEnabled;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public void reload() {
        loadConfig();
        cacheConfigValues();
    }
}