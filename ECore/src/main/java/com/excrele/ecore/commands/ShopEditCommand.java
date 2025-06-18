package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopEditCommand implements CommandExecutor {
    private final Ecore plugin;

    public ShopEditCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /shopedit <buy|sell|quantity> <value>");
            return true;
        }

        Block target = player.getTargetBlock(null, 5);
        if (target == null || !(target.getState() instanceof Sign)) {
            player.sendMessage(ChatColor.RED + "Please look at a shop sign!");
            return true;
        }

        Sign sign = (Sign) target.getState();
        boolean isPlayerShop = sign.getLine(0).equals(ChatColor.BLUE + "[PShop]");
        boolean isAdminShop = sign.getLine(0).equals(ChatColor.GREEN + "[Admin Shop]");
        if (!isPlayerShop && !isAdminShop) {
            player.sendMessage(ChatColor.RED + "This is not a valid shop sign!");
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "buy":
                    double buyPrice = Double.parseDouble(args[1]);
                    if (buyPrice < 0) {
                        player.sendMessage(ChatColor.RED + "Buy price cannot be negative!");
                        return true;
                    }
                    plugin.getShopManager().editShopPrice(player, sign, isPlayerShop, true, buyPrice);
                    break;
                case "sell":
                    double sellPrice = Double.parseDouble(args[1]);
                    if (sellPrice < 0) {
                        player.sendMessage(ChatColor.RED + "Sell price cannot be negative!");
                        return true;
                    }
                    plugin.getShopManager().editShopPrice(player, sign, isPlayerShop, false, sellPrice);
                    break;
                case "quantity":
                    int quantity = Integer.parseInt(args[1]);
                    plugin.getShopManager().editShopQuantity(player, sign, isPlayerShop, quantity);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid option! Use: buy, sell, or quantity");
                    return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please enter a valid number!");
        }

        return true;
    }
}