package com.abishekbhusal.hideplayers.listeners;

import com.abishekbhusal.hideplayers.utils.Utils;
import com.abishekbhusal.hideplayers.utils.WorldGuardUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HidePlayersListener implements Listener {
    private final Map<UUID, String> playerRegions = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!Utils.isAllowedWorld(player)) return;
        Utils.giveToggleItemIfAbsent(player);
        Utils.updateToggleItemInInventory(player);

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            if (item == null || !Utils.isToggleItem(item)) return;

            if (!item.isSimilar(Utils.getToggleItem())) {
                Utils.updateToggleItemInInventory(player);
                return;
            }

            Utils.togglePlayerVisibility(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!Utils.isToggleItem(event.getCurrentItem())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!Utils.isToggleItem(event.getItemDrop().getItemStack())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only trigger if the player moves between blocks
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        String currentRegion = WorldGuardUtils.getCurrentRegion(player);
        UUID playerId = player.getUniqueId();

        // Get the previously tracked region
        String previousRegion = playerRegions.getOrDefault(playerId, "");

        if (currentRegion.equals(previousRegion)) {
            return;
        }

        playerRegions.put(playerId, currentRegion);

        if (WorldGuardUtils.isInForcedHiddenRegion(player)) {
            Utils.hideAllPlayers(player);
        } else {
            Utils.showAllPlayers(player);
        }
    }

}
