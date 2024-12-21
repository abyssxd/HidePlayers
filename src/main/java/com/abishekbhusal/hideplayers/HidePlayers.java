package com.abishekbhusal.hideplayers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class HidePlayers extends JavaPlugin implements Listener, TabExecutor {

    private FileConfiguration config;
    private FileConfiguration messages;
    private final Set<UUID> hiddenPlayers = new HashSet<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();
        loadMessagesFile();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (this.getCommand("hideplayers") != null) {
            this.getCommand("hideplayers").setExecutor(this);
            this.getCommand("hideplayers").setTabCompleter(this);
        }
        getLogger().info("HidePlayersPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
        getLogger().info("HidePlayersPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("player_only"));
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            if (!player.hasPermission("hideplayers.use")) {
                player.sendMessage(getMessage("no_permission"));
                return true;
            }
            togglePlayerVisibility(player);
            return true;
        } else if (args[0].equalsIgnoreCase("setitem")) {
            if (!player.hasPermission("hideplayers.admin")) {
                player.sendMessage(getMessage("no_permission"));
                return true;
            }
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                player.sendMessage(getMessage("setitem_no_item"));
                return true;
            }
            setToggleItemInConfig(inHand);
            player.sendMessage(getMessage("setitem_success"));
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "Usage: /hideplayers [toggle|setitem]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("toggle".startsWith(args[0].toLowerCase())) {
                completions.add("toggle");
            }
            if ("setitem".startsWith(args[0].toLowerCase())) {
                completions.add("setitem");
            }
        }
        return completions;
    }

    private void togglePlayerVisibility(Player player) {
        if (!isAllowedWorld(player)) {
            player.sendMessage(getMessage("disallowed_world"));
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

    private void hideAllPlayers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.hidePlayer(this, other);
            }
        }
    }

    private void showAllPlayers(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player)) {
                player.showPlayer(this, other);
            }
        }
    }

    private boolean isAllowedWorld(Player player) {
        if (!config.getBoolean("worlds.use-whitelist", false)) {
            return true;
        }
        List<String> whitelist = config.getStringList("worlds.whitelist");
        return whitelist.contains(player.getWorld().getName());
    }

    private void showBossBar(Player player) {
        if (!config.getBoolean("bossbar.enabled", false)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!bossBars.containsKey(uuid)) {
            String bossBarMessage = ChatColor.translateAlternateColorCodes('&', getMessage("bossbar_message"));
            BarColor color = BarColor.valueOf(config.getString("bossbar.color", "RED").toUpperCase());
            BarStyle style = BarStyle.valueOf(config.getString("bossbar.style", "SOLID").toUpperCase());
            BossBar bar = Bukkit.createBossBar(bossBarMessage, color, style);
            bar.setProgress(1.0);
            bar.addPlayer(player);
            bossBars.put(uuid, bar);
        }
    }

    private void removeBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = bossBars.get(uuid);
        if (bar != null) {
            bar.removeAll();
            bossBars.remove(uuid);
        }
    }

    private void setToggleItemInConfig(ItemStack item) {
        config.set("toggle-item.material", item.getType().name());
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                config.set("toggle-item.name", meta.getDisplayName());
            } else {
                config.set("toggle-item.name", "");
            }
            if (meta.hasLore()) {
                config.set("toggle-item.lore", meta.getLore());
            } else {
                config.set("toggle-item.lore", new ArrayList<>());
            }
        } else {
            config.set("toggle-item.name", "");
            config.set("toggle-item.lore", new ArrayList<>());
        }
        saveConfig();
    }

    private ItemStack getConfiguredToggleItem() {
        Material mat = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        String name = config.getString("toggle-item.name", "&aToggle Players");
        List<String> lore = config.getStringList("toggle-item.lore");
        boolean glow = config.getBoolean("toggle-item.glow", false);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            if (lore != null) {
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            meta.setLore(coloredLore);
            if (glow) {
                meta.addEnchant(Enchantment.LUCK, 1, true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isAllowedWorld(player)) {
            return;
        }
        boolean autoGive = config.getBoolean("auto-give.enabled", true);
        int slot = config.getInt("auto-give.slot", 8);
        if (!autoGive) return;
        ItemStack toggleItem = getConfiguredToggleItem();
        ItemStack inSlot = player.getInventory().getItem(slot);
        if (inSlot == null || inSlot.getType() == Material.AIR) {
            player.getInventory().setItem(slot, toggleItem);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING
                || event.getView().getTopInventory().getType() == InventoryType.PLAYER) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            boolean canMoveItem = config.getBoolean("options.movable", false);
            if (canMoveItem) return;
            ItemStack currentItem = event.getCurrentItem();
            if (isToggleItem(currentItem)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        boolean canDropItem = config.getBoolean("options.droppable", false);
        if (canDropItem) return;
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (isToggleItem(dropped)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            Player player = event.getPlayer();
            if (item == null) return;
            if (!player.hasPermission("hideplayers.use")){
                player.sendMessage(getMessage("no_permission"));
                return;
            }
            if (isToggleItem(item)) {
                if (!isAllowedWorld(player)) {
                    player.sendMessage(getMessage("disallowed_world"));
                    return;
                }
                togglePlayerVisibility(player);
                event.setCancelled(true);
            }
        }
    }

    private boolean isToggleItem(ItemStack item) {
        if (item == null) return false;
        Material configMaterial = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        if (item.getType() != configMaterial) return false;
        String configName = config.getString("toggle-item.name", "&aToggle Players");
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            String configNamePlain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', configName));
            return displayName.equals(configNamePlain);
        }
        return false;
    }

    private void loadMessagesFile() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&',
                messages.getString(key, "&cMissing message for key: " + key));
    }
}
