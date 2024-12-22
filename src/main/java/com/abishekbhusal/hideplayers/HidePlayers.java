package com.abishekbhusal.hideplayers;

import com.abishekbhusal.hideplayers.commands.HidePlayersCommand;
import com.abishekbhusal.hideplayers.listeners.HidePlayersListener;
import com.abishekbhusal.hideplayers.utils.FileUpdater;
import com.abishekbhusal.hideplayers.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class HidePlayers extends JavaPlugin {

    private static HidePlayers instance;

    private FileConfiguration messages;

    @Override
    public void onEnable() {
        instance = this;

        FileUpdater.updateConfig();
        FileUpdater.updateMessages();

        reloadPlugin();

        getCommand("hideplayers").setExecutor(new HidePlayersCommand());
        getServer().getPluginManager().registerEvents(new HidePlayersListener(), this);

        getLogger().info("HidePlayers plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HidePlayers plugin has been disabled!");
    }

    public static HidePlayers getInstance() {
        return instance;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reloadPlugin() {
        // Reload configuration and messages
        reloadConfig();
        FileUpdater.updateConfig();
        FileUpdater.updateMessages();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));

        // Reload toggle item
        Utils.reloadToggleItem();

        // Update toggle item for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            Utils.updateToggleItemInInventory(player);
        }

        getLogger().info("HidePlayers configuration reloaded and toggle items updated.");
    }
}
