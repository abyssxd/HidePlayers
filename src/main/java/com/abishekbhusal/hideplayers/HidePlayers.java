package com.example.hideplayers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HidePlayers extends JavaPlugin implements Listener, TabExecutor {

    private FileConfiguration config;
    private final List<UUID> hiddenPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        Bukkit.getPluginManager().registerEvents(this, this);

        this.getCommand("hideplayers").setExecutor(this);
        this.getCommand("hideplayers").setTabCompleter(this);

        getLogger().info("HidePlayersPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
        getLogger().info("HidePlayersPlugin has been disabled!");
    }

    /**
     * Command structure:
     * 1) /hideplayers toggle (default action if none provided) - hides/shows players
     * 2) /hideplayers setitem - admin command to set the item from the player's hand
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            if (!player.hasPermission("hideplayers.use")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            togglePlayerVisibility(player);
            return true;
        }
        else if (args[0].equalsIgnoreCase("setitem")) {
            if (!player.hasPermission("hideplayers.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to set the item.");
                return true;
            }
            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "You must be holding a valid item to set.");
                return true;
            }
            setToggleItemInConfig(inHand);
            player.sendMessage(ChatColor.GREEN + "Hide/Show toggle item has been updated in the config!");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /hideplayers [toggle|setitem]");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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

    /**
     * Toggles whether the player sees others.
     * If the player is currently seeing others, we hide them. Otherwise, we show them again.
     */
    private void togglePlayerVisibility(Player player) {
        UUID uuid = player.getUniqueId();
        boolean currentlyHidden = hiddenPlayers.contains(uuid);

        if (currentlyHidden) {
            showAllPlayers(player);
            hiddenPlayers.remove(uuid);
            player.sendMessage(ChatColor.GREEN + "You can now see all players!");
        } else {
            hideAllPlayers(player);
            hiddenPlayers.add(uuid);
            player.sendMessage(ChatColor.GREEN + "All players hidden!");
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

    /**
     * Sets the toggle item in the config based on the ItemStack the admin is holding.
     */
    private void setToggleItemInConfig(ItemStack item) {
        config.set("toggle-item.material", item.getType().name());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            // Name
            if (meta.hasDisplayName()) {
                config.set("toggle-item.name", meta.getDisplayName());
            } else {
                config.set("toggle-item.name", "");
            }

            // Lore
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

    /**
     * Returns the ItemStack as defined in config.
     */
    private ItemStack getConfiguredToggleItem() {
        Material mat = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        String name = config.getString("toggle-item.name", "&aToggle Players");
        List<String> lore = config.getStringList("toggle-item.lore");

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Convert color codes
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> coloredLore = new ArrayList<>();
            if (lore != null) {
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
            meta.setLore(coloredLore);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * When player joins, if auto-give is enabled, we give them the toggle item in the specified slot
     * if they don't already have it in that slot.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean autoGive = config.getBoolean("auto-give.enabled", true);
        int slot = config.getInt("auto-give.slot", 8); // default to hotbar slot 8

        if (!autoGive) return;

        ItemStack toggleItem = getConfiguredToggleItem();
        // Check if the slot already has the item
        ItemStack inSlot = player.getInventory().getItem(slot);
        if (inSlot == null || inSlot.getType() == Material.AIR) {
            player.getInventory().setItem(slot, toggleItem);
        }
    }

    /**
     * Prevent item movement if disabled in config.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING ||
                event.getView().getTopInventory().getType() == InventoryType.PLAYER) {

            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            boolean canMoveItem = config.getBoolean("options.movable", false);
            if (canMoveItem) return;

            // Check if the clicked item is our toggle item
            ItemStack currentItem = event.getCurrentItem();
            if (isToggleItem(currentItem)) {
                // Cancel event if not movable
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    /**
     * Prevent dropping item if disabled in config.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        boolean canDropItem = config.getBoolean("options.droppable", false);
        if (canDropItem) return;

        ItemStack dropped = event.getItemDrop().getItemStack();
        if (isToggleItem(dropped)) {
            event.setCancelled(true);
        }
    }

    /**
     * Helper to check if an ItemStack matches our configured item (material, name).
     */
    private boolean isToggleItem(ItemStack item) {
        if (item == null) return false;

        // Check material
        Material configMaterial = Material.valueOf(config.getString("toggle-item.material", "BLAZE_ROD"));
        if (item.getType() != configMaterial) return false;

        // Check display name
        String configName = config.getString("toggle-item.name", "&aToggle Players");
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            String configNamePlain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', configName));
            return displayName.equals(configNamePlain);
        }

        return false;
    }
}
