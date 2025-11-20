package com.excrele.ecore.commands;

import com.excrele.ecore.Ecore;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimeWeatherCommand implements CommandExecutor {
    private final Ecore plugin;

    public TimeWeatherCommand(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        World world = null;

        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else if (args.length > 0) {
            world = plugin.getServer().getWorld(args[0]);
        }

        if (world == null && sender instanceof Player) {
            world = ((Player) sender).getWorld();
        }

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "No world specified!");
            return true;
        }

        switch (cmd) {
            case "time":
                return handleTime(sender, args, world);
            case "day":
                plugin.getTimeWeatherManager().setDay(world);
                sender.sendMessage(ChatColor.GREEN + "Set time to day in " + world.getName() + "!");
                return true;
            case "night":
                plugin.getTimeWeatherManager().setNight(world);
                sender.sendMessage(ChatColor.GREEN + "Set time to night in " + world.getName() + "!");
                return true;
            case "weather":
                return handleWeather(sender, args, world);
            case "sun":
            case "clear":
                plugin.getTimeWeatherManager().setSun(world);
                sender.sendMessage(ChatColor.GREEN + "Cleared weather in " + world.getName() + "!");
                return true;
            case "rain":
                plugin.getTimeWeatherManager().setRain(world);
                sender.sendMessage(ChatColor.GREEN + "Set rain in " + world.getName() + "!");
                return true;
            case "storm":
            case "thunder":
                plugin.getTimeWeatherManager().setStorm(world);
                sender.sendMessage(ChatColor.GREEN + "Set storm in " + world.getName() + "!");
                return true;
        }

        return false;
    }

    private boolean handleTime(CommandSender sender, String[] args) {
        return handleTime(sender, args, null);
    }

    private boolean handleTime(CommandSender sender, String[] args, World world) {
        if (!sender.hasPermission("ecore.time")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (world == null) {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                sender.sendMessage(ChatColor.RED + "No world specified!");
                return true;
            }
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /time <set|add> <value>");
            return true;
        }

        String action = args[0].toLowerCase();
        if (action.equals("set")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /time set <value>");
                return true;
            }
            long time;
            try {
                time = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid time value!");
                return true;
            }
            plugin.getTimeWeatherManager().setTime(world, time);
            sender.sendMessage(ChatColor.GREEN + "Set time to " + time + " in " + world.getName() + "!");
        } else if (action.equals("add")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /time add <value>");
                return true;
            }
            long amount;
            try {
                amount = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid time value!");
                return true;
            }
            plugin.getTimeWeatherManager().addTime(world, amount);
            sender.sendMessage(ChatColor.GREEN + "Added " + amount + " ticks to time in " + world.getName() + "!");
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /time <set|add> <value>");
            return true;
        }
        return true;
    }

    private boolean handleWeather(CommandSender sender, String[] args, World world) {
        if (!sender.hasPermission("ecore.weather")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /weather <clear|rain|storm>");
            return true;
        }

        String weather = args[0].toLowerCase();
        switch (weather) {
            case "clear":
            case "sun":
                plugin.getTimeWeatherManager().setSun(world);
                sender.sendMessage(ChatColor.GREEN + "Cleared weather in " + world.getName() + "!");
                break;
            case "rain":
                plugin.getTimeWeatherManager().setRain(world);
                sender.sendMessage(ChatColor.GREEN + "Set rain in " + world.getName() + "!");
                break;
            case "storm":
            case "thunder":
                plugin.getTimeWeatherManager().setStorm(world);
                sender.sendMessage(ChatColor.GREEN + "Set storm in " + world.getName() + "!");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /weather <clear|rain|storm>");
                return true;
        }
        return true;
    }
}

