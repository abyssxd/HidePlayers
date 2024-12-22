package com.abishekbhusal.hideplayers.commands;

import com.abishekbhusal.hideplayers.HidePlayers;
import com.abishekbhusal.hideplayers.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class HidePlayersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.getMessage("player_only"));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("hideplayers.use")) {
                player.sendMessage(Utils.getMessage("no_permission"));
                return true;
            }

            Utils.togglePlayerVisibility(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("setitem")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.getMessage("player_only"));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("hideplayers.admin")) {
                player.sendMessage(Utils.getMessage("no_permission"));
                return true;
            }

            ItemStack inHand = player.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                player.sendMessage(Utils.getMessage("setitem_no_item"));
                return true;
            }

            Utils.setToggleItemInConfig(inHand);
            player.sendMessage(Utils.getMessage("setitem_success"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hideplayers.reload")) {
                sender.sendMessage(Utils.getMessage("no_permission"));
                return true;
            }

            HidePlayers.getInstance().reloadPlugin();
            sender.sendMessage(Utils.getMessage("reload_success"));
            return true;
        }

        sender.sendMessage(Utils.getMessage("command_usage"));
        return true;
    }
}
