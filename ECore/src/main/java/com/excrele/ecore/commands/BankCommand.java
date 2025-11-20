package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BankCommand implements CommandExecutor {
    private final Ecore plugin;

    public BankCommand(Ecore plugin) {
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
            player.sendMessage(ChatColor.YELLOW + "=== Bank Commands ===");
            player.sendMessage(ChatColor.GREEN + "/bank create <name> - Create a bank account");
            player.sendMessage(ChatColor.GREEN + "/bank delete <name> - Delete a bank account");
            player.sendMessage(ChatColor.GREEN + "/bank list - List your bank accounts");
            player.sendMessage(ChatColor.GREEN + "/bank balance [account] - Check balance");
            player.sendMessage(ChatColor.GREEN + "/bank deposit <account> <amount> - Deposit money");
            player.sendMessage(ChatColor.GREEN + "/bank withdraw <account> <amount> - Withdraw money");
            player.sendMessage(ChatColor.GREEN + "/bank transfer <from> <to> <amount> - Transfer between accounts");
            player.sendMessage(ChatColor.GREEN + "/bank interest <account> [rate] - View or set interest rate");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "delete":
                return handleDelete(player, args);
            case "list":
                return handleList(player);
            case "balance":
            case "bal":
                return handleBalance(player, args);
            case "deposit":
            case "dep":
                return handleDeposit(player, args);
            case "withdraw":
            case "with":
                return handleWithdraw(player, args);
            case "transfer":
                return handleTransfer(player, args);
            case "interest":
                return handleInterest(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand! Use /bank for help.");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bank create <account-name>");
            return true;
        }

        String accountName = args[1];
        if (plugin.getBankManager().createAccount(player, accountName)) {
            player.sendMessage(ChatColor.GREEN + "Bank account '" + accountName + "' created successfully!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to create account. It may already exist or you've reached the maximum number of accounts!");
        }
        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bank delete <account-name>");
            return true;
        }

        String accountName = args[1];
        if (plugin.getBankManager().deleteAccount(player, accountName)) {
            player.sendMessage(ChatColor.GREEN + "Bank account '" + accountName + "' deleted. Balance returned to wallet.");
        } else {
            player.sendMessage(ChatColor.RED + "Account not found!");
        }
        return true;
    }

    private boolean handleList(Player player) {
        List<String> accounts = plugin.getBankManager().getAccountNames(player);
        if (accounts.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You have no bank accounts. Create one with /bank create <name>");
        } else {
            player.sendMessage(ChatColor.GREEN + "Your bank accounts:");
            for (String account : accounts) {
                double balance = plugin.getBankManager().getBalance(player, account);
                double interestRate = plugin.getBankManager().getInterestRate(player, account);
                player.sendMessage(ChatColor.GRAY + "  - " + ChatColor.YELLOW + account + 
                    ChatColor.GRAY + ": " + ChatColor.GREEN + String.format("%.2f", balance) +
                    ChatColor.GRAY + " (Interest: " + String.format("%.2f%%", interestRate * 100) + ")");
            }
            player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.GREEN + 
                String.format("%.2f", plugin.getBankManager().getTotalBalance(player)));
        }
        return true;
    }

    private boolean handleBalance(Player player, String[] args) {
        if (args.length == 1) {
            // Show all accounts
            handleList(player);
            return true;
        }

        String accountName = args[1];
        double balance = plugin.getBankManager().getBalance(player, accountName);
        if (balance == 0.0 && !plugin.getBankManager().getAccountNames(player).contains(accountName)) {
            player.sendMessage(ChatColor.RED + "Account not found!");
            return true;
        }

        double interestRate = plugin.getBankManager().getInterestRate(player, accountName);
        player.sendMessage(ChatColor.GREEN + "Account '" + accountName + "' balance: " + 
            ChatColor.YELLOW + String.format("%.2f", balance));
        player.sendMessage(ChatColor.GRAY + "Interest rate: " + String.format("%.2f%%", interestRate * 100));
        return true;
    }

    private boolean handleDeposit(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bank deposit <account> <amount>");
            return true;
        }

        String accountName = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }

            if (plugin.getBankManager().deposit(player, accountName, amount)) {
                player.sendMessage(ChatColor.GREEN + "Deposited " + String.format("%.2f", amount) + 
                    " into account '" + accountName + "'!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to deposit. Check that you have enough money and the account exists!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
        }
        return true;
    }

    private boolean handleWithdraw(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bank withdraw <account> <amount>");
            return true;
        }

        String accountName = args[1];
        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }

            if (plugin.getBankManager().withdraw(player, accountName, amount)) {
                player.sendMessage(ChatColor.GREEN + "Withdrew " + String.format("%.2f", amount) + 
                    " from account '" + accountName + "'!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to withdraw. Check that you have enough money in the account!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
        }
        return true;
    }

    private boolean handleTransfer(Player player, String[] args) {
        if (args.length != 4) {
            player.sendMessage(ChatColor.RED + "Usage: /bank transfer <from-account> <to-account> <amount>");
            return true;
        }

        String fromAccount = args[1];
        String toAccount = args[2];
        try {
            double amount = Double.parseDouble(args[3]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }

            if (plugin.getBankManager().transfer(player, fromAccount, toAccount, amount)) {
                player.sendMessage(ChatColor.GREEN + "Transferred " + String.format("%.2f", amount) + 
                    " from '" + fromAccount + "' to '" + toAccount + "'!");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to transfer. Check account names and balances!");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
        }
        return true;
    }

    private boolean handleInterest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bank interest <account> [rate]");
            return true;
        }

        String accountName = args[1];
        if (args.length == 2) {
            // View interest rate
            double rate = plugin.getBankManager().getInterestRate(player, accountName);
            player.sendMessage(ChatColor.GREEN + "Account '" + accountName + "' interest rate: " + 
                String.format("%.2f%%", rate * 100));
        } else {
            // Set interest rate (staff only)
            if (!player.hasPermission("ecore.bank.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to set interest rates!");
                return true;
            }

            try {
                double rate = Double.parseDouble(args[2]) / 100.0; // Convert percentage to decimal
                if (plugin.getBankManager().setInterestRate(player, accountName, rate)) {
                    player.sendMessage(ChatColor.GREEN + "Interest rate for '" + accountName + "' set to " + 
                        String.format("%.2f%%", rate * 100) + "!");
                } else {
                    player.sendMessage(ChatColor.RED + "Account not found!");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid interest rate! Use a number (e.g., 5 for 5%)");
            }
        }
        return true;
    }
}

