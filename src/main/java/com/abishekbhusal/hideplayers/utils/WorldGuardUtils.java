package com.abishekbhusal.hideplayers.utils;

import com.abishekbhusal.hideplayers.HidePlayers;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class WorldGuardUtils {

    public static String getCurrentRegion(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            return ""; // No region manager available
        }

        BlockVector3 location = BukkitAdapter.asBlockVector(player.getLocation());
        ApplicableRegionSet regions = regionManager.getApplicableRegions(location);

        // Combine all region IDs into a single string (sorted for consistency)
        return regions.getRegions().stream()
                .map(ProtectedRegion::getId)
                .sorted()
                .collect(Collectors.joining(","));
    }


    public static boolean isInForcedHiddenRegion(Player player) {
        if (!HidePlayers.getInstance().getConfig().getBoolean("worldguard.enabled", false)) {
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(player.getWorld());
        RegionManager regionManager = container.get(world);

        if (regionManager == null) {
            return false;
        }

        BlockVector3 playerLocation = BukkitAdapter.asBlockVector(player.getLocation());
        ApplicableRegionSet regionSet = regionManager.getApplicableRegions(playerLocation);

        List<String> forcedRegions = HidePlayers.getInstance().getConfig().getStringList("worldguard.force-hidden-regions");

        for (ProtectedRegion region : regionSet) {
            if (forcedRegions.contains(region.getId())) {
                return true;
            }
        }

        return false;
    }
}
