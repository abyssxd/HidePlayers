package com.abishekbhusal.hideplayers.utils;

import com.abishekbhusal.hideplayers.HidePlayers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class Utils {

    private static final FileConfiguration config = HidePlayers.getInstance().getConfig();
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static final Set<UUID> hiddenPlayers = new HashSet<>();
    private static ItemStack toggleItem;
    private static final NamespacedKey TOGGLE_ITEM_KEY = new NamespacedKey(HidePlayers.getInstance(), "toggle-item");

    public static String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&',
                HidePlayers.getInstance().getMessages().getString(key, "&cMissing message for key: " + key));
    }

    public static boolean isAllowedWorld(Player player) {
        if (!config.getBoolean("worlds.use-whitelist", false)) return true;
        List<String> whitelist = config.getStringList("worlds.whitelist");
        return whitelist.contains(player.getWorld().getName());
    }

    public static void togglePlayerVisibility(Player player) {
        if (WorldGuardUtils.isInForcedHiddenRegion(player)) {
            player.sendMessage(getMessage("forced_hidden_region"));
            hideAllPlayers(player);
            removeBossBar(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean currentlyHidden = hiddenPlayers.contains(uuid);

        if (currentlyHidden) {
            showAllPlayers(player);
            hiddenPlayers.remove(uuid);
            player.sendMessage(getMessage("toggle_off"));
            removeBossBar(player);
        } else {
            hideAllPlayers(player);
            hiddenPlayers.add(uuid);
            player.sendMessage(getMessage("toggle_on"));
            showBossBar(player);
        }
    }


    public static void hideAllPlayers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.hidePlayer(HidePlayers.getInstance(), other);
            }
        }
    }

    public static void showAllPlayers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.showPlayer(HidePlayers.getInstance(), other);
            }
        }
    }

    public static void showBossBar(Player player) {
        String message = getMessage("bossbar_message");
        BarColor color = BarColor.valueOf(config.getString("bossbar.color", "RED").toUpperCase());
        BarStyle style = BarStyle.valueOf(config.getString("bossbar.style", "SOLID").toUpperCase());

        BossBar bar = Bukkit.createBossBar(message, color, style);
        bar.setProgress(1.0);
        bar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bar);
    }

    public static void removeBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();
    }

    public static void setToggleItemInConfig(ItemStack item) {
        config.set("toggle-item.material", item.getType().name());
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            config.set("toggle-item.name", meta.getDisplayName());
        }
    }


    public static ItemStack createToggleItem() {
        Material material = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        String name = config.getString("toggle-item.name", "&aToggle Players");
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        }

        return item;
    }


    public static void reloadToggleItem() {
        Material material = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        String name = config.getString("toggle-item.name", "&aToggle Players");
        List<String> lore = config.getStringList("toggle-item.lore");
        boolean glow = config.getBoolean("toggle-item.glow", false);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(TOGGLE_ITEM_KEY, PersistentDataType.STRING, "true");

            if (glow) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
            }

            item.setItemMeta(meta);
        }

        toggleItem = item;
    }

    public static ItemStack getToggleItem() {
        return toggleItem.clone();
    }

    public static boolean isToggleItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(TOGGLE_ITEM_KEY, PersistentDataType.STRING);
    }

    public static void giveToggleItemIfAbsent(Player player) {
        int slot = config.getInt("auto-give.slot", 8);
        ItemStack inSlot = player.getInventory().getItem(slot);

        if (inSlot == null || !isToggleItem(inSlot)) {
            player.getInventory().setItem(slot, toggleItem.clone());
        }
    }

    public static void updateToggleItemInInventory(Player player) {
        ItemStack currentToggleItem = getToggleItem();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isToggleItem(item) && !item.isSimilar(currentToggleItem)) {
                player.getInventory().setItem(i, currentToggleItem.clone());
            }
        }
    }


}
