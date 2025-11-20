package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for vault system.
 */
public class VaultCommand implements CommandExecutor, TabCompleter {
    private final Ecore plugin;

    public VaultCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            plugin.getVaultGUIManager().openVaultSelectionGUI(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "open":
            case "o":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault open <number>");
                    return true;
                }
                try {
                    int vaultNumber = Integer.parseInt(args[1]);
                    plugin.getVaultGUIManager().openVault(player, vaultNumber);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid vault number!");
                }
                break;
            case "create":
            case "c":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault create <number>");
                    return true;
                }
                try {
                    int vaultNumber = Integer.parseInt(args[1]);
                    if (plugin.getVaultManager().createVault(player, vaultNumber)) {
                        player.sendMessage(ChatColor.GREEN + "Vault #" + vaultNumber + " created!");
                        plugin.getVaultGUIManager().openVault(player, vaultNumber);
                    } else {
                        player.sendMessage(ChatColor.RED + "Cannot create vault. You may have reached the maximum number of vaults or this vault already exists.");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid vault number!");
                }
                break;
            case "rename":
            case "r":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault rename <number> <name>");
                    return true;
                }
                try {
                    int vaultNumber = Integer.parseInt(args[1]);
                    if (!plugin.getVaultManager().hasVault(player, vaultNumber)) {
                        player.sendMessage(ChatColor.RED + "Vault #" + vaultNumber + " does not exist!");
                        return true;
                    }
                    StringBuilder nameBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        if (i > 2) nameBuilder.append(" ");
                        nameBuilder.append(args[i]);
                    }
                    String name = ChatColor.translateAlternateColorCodes('&', nameBuilder.toString());
                    plugin.getVaultManager().setVaultName(player, vaultNumber, name);
                    player.sendMessage(ChatColor.GREEN + "Vault #" + vaultNumber + " renamed to: " + name);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid vault number!");
                }
                break;
            case "trust":
            case "t":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault trust <number> <player>");
                    return true;
                }
                try {
                    int vaultNumber = Integer.parseInt(args[1]);
                    if (!plugin.getVaultManager().hasVault(player, vaultNumber)) {
                        player.sendMessage(ChatColor.RED + "Vault #" + vaultNumber + " does not exist!");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                        return true;
                    }
                    plugin.getVaultManager().addTrustedPlayer(player, vaultNumber, target.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + target.getName() + " can now access Vault #" + vaultNumber);
                    target.sendMessage(ChatColor.GREEN + player.getName() + " granted you access to Vault #" + vaultNumber);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid vault number!");
                }
                break;
            case "untrust":
            case "ut":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault untrust <number> <player>");
                    return true;
                }
                try {
                    int vaultNumber = Integer.parseInt(args[1]);
                    if (!plugin.getVaultManager().hasVault(player, vaultNumber)) {
                        player.sendMessage(ChatColor.RED + "Vault #" + vaultNumber + " does not exist!");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                        return true;
                    }
                    plugin.getVaultManager().removeTrustedPlayer(player, vaultNumber, target.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + target.getName() + " can no longer access Vault #" + vaultNumber);
                    target.sendMessage(ChatColor.RED + player.getName() + " revoked your access to Vault #" + vaultNumber);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid vault number!");
                }
                break;
            case "list":
            case "l":
                handleList(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /vault for GUI or:");
                sendHelp(player);
        }

        return true;
    }

    private void handleList(Player player) {
        List<Integer> vaults = plugin.getVaultManager().getPlayerVaults(player);
        int maxVaults = plugin.getVaultManager().getMaxVaults(player);
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "          Your Vaults (" + vaults.size() + "/" + maxVaults + ")");
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
        
        if (vaults.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "You have no vaults. Use /vault create <number> to create one!");
        } else {
            for (int vaultNum : vaults) {
                String vaultName = plugin.getVaultManager().getVaultName(player, vaultNum);
                player.sendMessage(ChatColor.WHITE + "Vault #" + vaultNum + ": " + ChatColor.YELLOW + vaultName);
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "═══════════════════════════════════════");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Vault Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/vault - Open vault selection GUI");
        player.sendMessage(ChatColor.YELLOW + "/vault open <number> - Open specific vault");
        player.sendMessage(ChatColor.YELLOW + "/vault create <number> - Create new vault");
        player.sendMessage(ChatColor.YELLOW + "/vault rename <number> <name> - Rename vault");
        player.sendMessage(ChatColor.YELLOW + "/vault trust <number> <player> - Trust player");
        player.sendMessage(ChatColor.YELLOW + "/vault untrust <number> <player> - Untrust player");
        player.sendMessage(ChatColor.YELLOW + "/vault list - List your vaults");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("open", "create", "rename", "trust", "untrust", "list")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || 
                                 args[0].equalsIgnoreCase("create") ||
                                 args[0].equalsIgnoreCase("rename") ||
                                 args[0].equalsIgnoreCase("trust") ||
                                 args[0].equalsIgnoreCase("untrust"))) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<Integer> vaults = plugin.getVaultManager().getPlayerVaults(player);
                return vaults.stream()
                        .map(String::valueOf)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("trust") || 
                                 args[0].equalsIgnoreCase("untrust"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

