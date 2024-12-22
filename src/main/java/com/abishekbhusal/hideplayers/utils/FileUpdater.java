package com.abishekbhusal.hideplayers.utils;

import com.abishekbhusal.hideplayers.HidePlayers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class FileUpdater {

    public static void updateFile(File file, String resourceName) {
        HidePlayers plugin = HidePlayers.getInstance();

        if (!file.exists()) {
            plugin.saveResource(resourceName, false);
            return;
        }

        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(resourceName);
        if (defaultStream == null) return;

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));

        boolean updated = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (!existingConfig.contains(key)) {
                existingConfig.set(key, defaultConfig.get(key));
                updated = true;
            }
        }

        if (updated) {
            try {
                existingConfig.save(file);
                plugin.getLogger().info(resourceName + " has been updated with missing keys.");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not update " + resourceName + ": " + e.getMessage());
            }
        }
    }

    public static void updateConfig() {
        File configFile = new File(HidePlayers.getInstance().getDataFolder(), "config.yml");
        updateFile(configFile, "config.yml");
    }

    public static void updateMessages() {
        File messagesFile = new File(HidePlayers.getInstance().getDataFolder(), "messages.yml");
        updateFile(messagesFile, "messages.yml");
    }
}
