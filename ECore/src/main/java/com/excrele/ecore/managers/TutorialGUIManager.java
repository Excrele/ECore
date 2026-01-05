package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI manager for the tutorial system.
 * Displays all plugin commands with pagination, nesting, and detailed information.
 */
public class TutorialGUIManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, TutorialState> playerStates;
    private final List<CommandInfo> commands;

    public TutorialGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.playerStates = new HashMap<>();
        this.commands = initializeCommands();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the main tutorial GUI showing all command categories.
     */
    public void openMainGUI(Player player) {
        playerStates.put(player.getUniqueId(), new TutorialState(null, 0, null));
        openGUI(player, null, 0);
    }

    /**
     * Opens a GUI for a specific command category or subcommands.
     */
    private void openGUI(Player player, String parentCommand, int page) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null) {
            state = new TutorialState(parentCommand, page, null);
            playerStates.put(player.getUniqueId(), state);
        } else {
            state.currentParent = parentCommand;
            state.currentPage = page;
        }

        List<CommandInfo> itemsToShow = getCommandsForView(parentCommand);
        int itemsPerPage = 45; // 5 rows of 9 items
        int totalPages = (int) Math.ceil((double) itemsToShow.size() / itemsPerPage);
        
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, itemsToShow.size());

        int size = 54; // 6 rows
        String title = parentCommand == null 
            ? ChatColor.DARK_AQUA + "Ecore Tutorial - Page " + (page + 1) + "/" + Math.max(1, totalPages)
            : ChatColor.DARK_AQUA + "Commands: " + parentCommand + " - Page " + (page + 1) + "/" + Math.max(1, totalPages);
        
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Add command items
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            CommandInfo cmd = itemsToShow.get(i);
            ItemStack item = createCommandItem(cmd, parentCommand != null);
            gui.setItem(slot++, item);
        }

        // Add navigation buttons
        addNavigationButtons(gui, page, totalPages, parentCommand != null);

        player.openInventory(gui);
    }

    /**
     * Opens detailed information GUI for a command.
     */
    private void openDetailGUI(Player player, CommandInfo command) {
        TutorialState state = playerStates.get(player.getUniqueId());
        if (state != null) {
            state.detailCommand = command;
        }

        int size = 54;
        String title = ChatColor.DARK_PURPLE + "Details: " + command.name;
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Command name and description
        ItemStack nameItem = createMenuItem(Material.BOOK, 
            ChatColor.GOLD + "Command: " + ChatColor.WHITE + "/" + command.name,
            Arrays.asList(
                ChatColor.GRAY + "Description:",
                ChatColor.WHITE + command.description
            ));
        gui.setItem(4, nameItem);

        // Usage
        ItemStack usageItem = createMenuItem(Material.PAPER,
            ChatColor.YELLOW + "Usage",
            Arrays.asList(
                ChatColor.GRAY + command.usage
            ));
        gui.setItem(13, usageItem);

        // Permission
        ItemStack permItem = createMenuItem(Material.SHIELD,
            ChatColor.BLUE + "Permission",
            Arrays.asList(
                ChatColor.GRAY + command.permission != null ? command.permission : "None required"
            ));
        gui.setItem(22, permItem);

        // Aliases
        if (!command.aliases.isEmpty()) {
            List<String> aliasLore = new ArrayList<>();
            aliasLore.add(ChatColor.GRAY + "Aliases:");
            for (String alias : command.aliases) {
                aliasLore.add(ChatColor.WHITE + "/" + alias);
            }
            ItemStack aliasItem = createMenuItem(Material.NAME_TAG,
                ChatColor.GREEN + "Aliases",
                aliasLore);
            gui.setItem(31, aliasItem);
        }

        // Subcommands
        if (!command.subcommands.isEmpty()) {
            List<String> subLore = new ArrayList<>();
            subLore.add(ChatColor.GRAY + "Subcommands:");
            for (CommandInfo sub : command.subcommands) {
                subLore.add(ChatColor.WHITE + "- " + sub.name + ": " + ChatColor.GRAY + sub.description);
            }
            ItemStack subItem = createMenuItem(Material.COMPASS,
                ChatColor.AQUA + "Subcommands",
                subLore);
            gui.setItem(40, subItem);
        }

        // Additional details
        if (command.details != null && !command.details.isEmpty()) {
            List<String> detailLore = new ArrayList<>();
            detailLore.add(ChatColor.GRAY + "Additional Information:");
            for (String detail : command.details) {
                detailLore.add(ChatColor.WHITE + detail);
            }
            ItemStack detailItem = createMenuItem(Material.KNOWLEDGE_BOOK,
                ChatColor.LIGHT_PURPLE + "Additional Info",
                detailLore);
            gui.setItem(49, detailItem);
        }

        // Back button
        ItemStack back = createMenuItem(Material.ARROW,
            ChatColor.YELLOW + "Back",
            Arrays.asList(ChatColor.GRAY + "Return to previous menu"));
        gui.setItem(45, back);

        // Fill empty slots
        fillEmptySlots(gui);

        player.openInventory(gui);
    }

    /**
     * Creates an item representing a command.
     */
    private ItemStack createCommandItem(CommandInfo command, boolean isSubcommand) {
        Material material = command.subcommands.isEmpty() ? Material.COMMAND_BLOCK : Material.CHAIN_COMMAND_BLOCK;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName((isSubcommand ? ChatColor.GRAY : ChatColor.AQUA) + "/" + command.name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + command.description);
        lore.add("");
        lore.add(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + command.usage);
        
        if (command.permission != null) {
            lore.add(ChatColor.DARK_GRAY + "Permission: " + ChatColor.RED + command.permission);
        }
        
        if (!command.subcommands.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-click to view subcommands");
        }
        
        lore.add(ChatColor.GOLD + "Right-click for detailed info");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Adds navigation buttons to the GUI.
     */
    private void addNavigationButtons(Inventory gui, int currentPage, int totalPages, boolean hasParent) {
        // Previous page button
        if (currentPage > 0) {
            ItemStack prev = createMenuItem(Material.ARROW,
                ChatColor.YELLOW + "Previous Page",
                Arrays.asList(ChatColor.GRAY + "Page " + currentPage + "/" + totalPages));
            gui.setItem(45, prev);
        }

        // Back button (if in submenu) - center slot
        if (hasParent) {
            ItemStack back = createMenuItem(Material.BARRIER,
                ChatColor.RED + "Back to Main Menu",
                Arrays.asList(ChatColor.GRAY + "Return to command list"));
            gui.setItem(49, back);
        } else if (totalPages > 1) {
            // Page info (only if not in submenu and multiple pages)
            ItemStack info = createMenuItem(Material.PAPER,
                ChatColor.GRAY + "Page " + (currentPage + 1) + "/" + totalPages,
                Arrays.asList(ChatColor.GRAY + "Use arrows to navigate"));
            gui.setItem(49, info);
        }

        // Next page button
        if (currentPage < totalPages - 1) {
            ItemStack next = createMenuItem(Material.ARROW,
                ChatColor.YELLOW + "Next Page",
                Arrays.asList(ChatColor.GRAY + "Page " + (currentPage + 2) + "/" + totalPages));
            gui.setItem(53, next);
        }
    }

    /**
     * Fills empty slots with glass panes.
     */
    private void fillEmptySlots(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
    }

    /**
     * Creates a menu item with display name and lore.
     */
    private ItemStack createMenuItem(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Gets commands to display based on the current view.
     */
    private List<CommandInfo> getCommandsForView(String parentCommand) {
        if (parentCommand == null) {
            // Return top-level commands (those without parents)
            List<CommandInfo> topLevel = new ArrayList<>();
            for (CommandInfo cmd : commands) {
                if (cmd.parent == null) {
                    topLevel.add(cmd);
                }
            }
            return topLevel;
        } else {
            // Return subcommands of the parent
            for (CommandInfo cmd : commands) {
                if (cmd.name.equals(parentCommand)) {
                    return cmd.subcommands;
                }
            }
            return new ArrayList<>();
        }
    }

    /**
     * Finds a command by name.
     */
    private CommandInfo findCommand(String name) {
        // First check top-level commands
        for (CommandInfo cmd : commands) {
            if (cmd.name.equals(name)) {
                return cmd;
            }
            // Check aliases
            if (cmd.aliases.contains(name)) {
                return cmd;
            }
            // Check subcommands
            for (CommandInfo sub : cmd.subcommands) {
                if (sub.name.equals(name) || sub.aliases.contains(name)) {
                    return sub;
                }
            }
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        if (!title.contains("Ecore Tutorial") && !title.contains("Commands:") && !title.contains("Details:")) {
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        TutorialState state = playerStates.get(player.getUniqueId());
        if (state == null) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        // Handle navigation buttons
        if (clicked.getType() == Material.ARROW) {
            if (displayName.contains("Previous")) {
                openGUI(player, state.currentParent, state.currentPage - 1);
            } else if (displayName.contains("Next")) {
                openGUI(player, state.currentParent, state.currentPage + 1);
            } else if (displayName.contains("Back")) {
                if (state.currentParent != null) {
                    openGUI(player, null, 0);
                } else {
                    player.closeInventory();
                }
            }
            return;
        }

        if (clicked.getType() == Material.BARRIER && displayName.contains("Back")) {
            openGUI(player, null, 0);
            return;
        }

        // Handle detail view back button
        if (title.contains("Details:")) {
            if (displayName.contains("Back") || clicked.getType() == Material.ARROW) {
                if (state.currentParent != null) {
                    openGUI(player, state.currentParent, state.currentPage);
                } else {
                    openGUI(player, null, state.currentPage);
                }
            }
            return;
        }

        // Handle command clicks
        if (clicked.getType() == Material.COMMAND_BLOCK || clicked.getType() == Material.CHAIN_COMMAND_BLOCK) {
            String commandName = displayName.replace("/", "");
            
            if (event.isRightClick()) {
                // Open detailed view
                CommandInfo cmd = findCommand(commandName);
                if (cmd != null) {
                    openDetailGUI(player, cmd);
                }
            } else {
                // Left click - check if has subcommands
                CommandInfo cmd = findCommand(commandName);
                if (cmd != null && !cmd.subcommands.isEmpty()) {
                    openGUI(player, commandName, 0);
                }
            }
        }
    }

    /**
     * Initializes all plugin commands with their information.
     */
    private List<CommandInfo> initializeCommands() {
        List<CommandInfo> cmds = new ArrayList<>();

        // Home Commands
        CommandInfo home = new CommandInfo("home", "Teleport to a home", "/home [name]", "ecore.home", null);
        home.addSubcommand(new CommandInfo("sethome", "Set a home location", "/sethome <name>", "ecore.home", home));
        home.addSubcommand(new CommandInfo("listhomes", "List all your homes", "/listhomes", "ecore.home", home));
        home.addSubcommand(new CommandInfo("homeshare", "Share a home with a player", "/homeshare <home> <player>", "ecore.home", home));
        home.addSubcommand(new CommandInfo("homeunshare", "Unshare a home", "/homeunshare <home> <player>", "ecore.home", home));
        home.addSubcommand(new CommandInfo("homecategory", "Set home category", "/homecategory <home> <category>", "ecore.home", home));
        home.addSubcommand(new CommandInfo("homeicon", "Set home icon", "/homeicon <home> <material>", "ecore.home", home));
        home.addSubcommand(new CommandInfo("homedescription", "Set home description", "/homedescription <home> <description>", "ecore.home", home));
        cmds.add(home);

        // Economy Commands
        CommandInfo economy = new CommandInfo("economy", "Economy management system", "/economy <give|take|set|stats> [player] [amount]", "ecore.economy.admin", null);
        economy.addSubcommand(new CommandInfo("balance", "Check your balance", "/balance [player]", "ecore.economy", economy));
        economy.addAlias("bal");
        economy.addAlias("money");
        economy.addSubcommand(new CommandInfo("pay", "Pay a player", "/pay <player> <amount>", "ecore.economy", economy));
        economy.addSubcommand(new CommandInfo("baltop", "View economy leaderboard", "/baltop [limit]", "ecore.economy", economy));
        economy.addAlias("balancetop");
        cmds.add(economy);

        // Teleport Commands
        CommandInfo teleport = new CommandInfo("teleport", "Teleportation system", "/teleport <player> or /teleport <x> <y> <z>", "ecore.teleport", null);
        teleport.addAlias("tp");
        teleport.addSubcommand(new CommandInfo("tpa", "Request to teleport to a player", "/tpa <player>", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("tpahere", "Request a player to teleport to you", "/tpahere <player>", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("tpaccept", "Accept a teleport request", "/tpaccept", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("tpdeny", "Deny a teleport request", "/tpdeny", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("back", "Return to previous location", "/back", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("top", "Teleport to highest block", "/top", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("jump", "Teleport forward", "/jump", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("rtp", "Random teleport", "/rtp", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("tpbiome", "Teleport to a biome", "/tpbiome <biome>", "ecore.teleport", teleport));
        teleport.addSubcommand(new CommandInfo("tpstructure", "Teleport to a structure", "/tpstructure <structure>", "ecore.teleport", teleport));
        cmds.add(teleport);

        // Warp Commands
        CommandInfo warp = new CommandInfo("warp", "Teleport to a warp", "/warp <name>", "ecore.warp", null);
        warp.addSubcommand(new CommandInfo("setwarp", "Create a warp", "/setwarp <name>", "ecore.warp.set", warp));
        warp.addSubcommand(new CommandInfo("delwarp", "Delete a warp", "/delwarp <name>", "ecore.warp.delete", warp));
        warp.addSubcommand(new CommandInfo("warps", "List all warps", "/warps", "ecore.warp", warp));
        cmds.add(warp);

        // Spawn Commands
        CommandInfo spawn = new CommandInfo("spawn", "Teleport to spawn", "/spawn [player]", "ecore.spawn", null);
        spawn.addSubcommand(new CommandInfo("setspawn", "Set the spawn point", "/setspawn", "ecore.spawn.set", spawn));
        cmds.add(spawn);

        // Kit Commands
        CommandInfo kit = new CommandInfo("kit", "Get a kit", "/kit <name> or /kit list", "ecore.kit", null);
        kit.addDetail("Use /kit list to see all available kits");
        cmds.add(kit);

        // Mail Commands
        CommandInfo mail = new CommandInfo("mail", "Mail system", "/mail <send|read|clear> [args]", "ecore.mail", null);
        mail.addDetail("Send mail: /mail send <player> <message>");
        mail.addDetail("Read mail: /mail read");
        mail.addDetail("Clear mail: /mail clear");
        cmds.add(mail);

        // Bank Commands
        CommandInfo bank = new CommandInfo("bank", "Bank account management", "/bank <create|delete|list|balance|deposit|withdraw|transfer|interest> [args]", "ecore.economy", null);
        bank.addDetail("Create account: /bank create");
        bank.addDetail("Deposit money: /bank deposit <amount>");
        bank.addDetail("Withdraw money: /bank withdraw <amount>");
        cmds.add(bank);

        // Statistics Commands
        CommandInfo stats = new CommandInfo("stats", "View your statistics", "/stats", "ecore.economy", null);
        stats.addAlias("statistics");
        stats.addSubcommand(new CommandInfo("leaderboard", "View leaderboards", "/leaderboard [stat]", "ecore.economy", stats));
        stats.addAlias("lb");
        cmds.add(stats);

        // Achievement Commands
        CommandInfo achievement = new CommandInfo("achievements", "View your achievements", "/achievements [list|give|check]", "ecore.economy", null);
        achievement.addAlias("achievement");
        cmds.add(achievement);

        // Auction House Commands
        CommandInfo ah = new CommandInfo("auctionhouse", "Auction house system", "/auctionhouse [create|bid|buyout|cancel|list|my]", "ecore.economy", null);
        ah.addAlias("ah");
        ah.addAlias("auction");
        cmds.add(ah);

        // Player Info Commands
        CommandInfo playerInfo = new CommandInfo("whois", "Get player information", "/whois <player>", "ecore.economy", null);
        playerInfo.addAlias("seen");
        playerInfo.addSubcommand(new CommandInfo("list", "List online players", "/list", "ecore.economy", playerInfo));
        playerInfo.addAlias("who");
        playerInfo.addSubcommand(new CommandInfo("ping", "Check your ping", "/ping [player]", "ecore.economy", playerInfo));
        playerInfo.addSubcommand(new CommandInfo("near", "Show nearby players", "/near [radius]", "ecore.economy", playerInfo));
        cmds.add(playerInfo);

        // Time/Weather Commands
        CommandInfo time = new CommandInfo("time", "Manage time", "/time <set|add> <value>", "ecore.time", null);
        time.addSubcommand(new CommandInfo("day", "Set time to day", "/day", "ecore.time", time));
        time.addSubcommand(new CommandInfo("night", "Set time to night", "/night", "ecore.time", time));
        time.addSubcommand(new CommandInfo("weather", "Manage weather", "/weather <clear|rain|storm>", "ecore.weather", time));
        time.addSubcommand(new CommandInfo("sun", "Clear weather", "/sun", "ecore.weather", time));
        time.addSubcommand(new CommandInfo("rain", "Set rain", "/rain", "ecore.weather", time));
        time.addSubcommand(new CommandInfo("storm", "Set storm", "/storm", "ecore.weather", time));
        cmds.add(time);

        // AFK Commands
        CommandInfo afk = new CommandInfo("afk", "Toggle AFK status", "/afk [player]", "ecore.afk", null);
        cmds.add(afk);

        // Jail Commands
        CommandInfo jail = new CommandInfo("jail", "Jail a player", "/jail <player> <jail> [time] [reason]", "ecore.jail", null);
        jail.addSubcommand(new CommandInfo("unjail", "Unjail a player", "/unjail <player>", "ecore.jail", jail));
        jail.addSubcommand(new CommandInfo("setjail", "Create a jail location", "/setjail <name>", "ecore.jail.set", jail));
        jail.addSubcommand(new CommandInfo("jailinfo", "Check jail information", "/jailinfo <player>", "ecore.jail", jail));
        cmds.add(jail);

        // Chat Commands
        CommandInfo chat = new CommandInfo("chat", "Manage chat", "/chat <on|off|clear>", "ecore.chat.manage", null);
        chat.addSubcommand(new CommandInfo("msg", "Send a private message", "/msg <player> <message>", "ecore.economy", chat));
        chat.addAlias("message");
        chat.addAlias("tell");
        chat.addAlias("whisper");
        chat.addSubcommand(new CommandInfo("reply", "Reply to last message", "/reply <message>", "ecore.economy", chat));
        chat.addAlias("r");
        chat.addSubcommand(new CommandInfo("staffchat", "Staff chat", "/staffchat <message>", "ecore.chat.manage", chat));
        chat.addAlias("sc");
        chat.addSubcommand(new CommandInfo("adminchat", "Admin chat", "/adminchat <message>", "ecore.chat.manage", chat));
        chat.addAlias("ac");
        cmds.add(chat);

        // Staff Commands
        CommandInfo staff = new CommandInfo("staff", "Staff moderation tools", "/ecore staff", "ecore.staff", null);
        staff.addSubcommand(new CommandInfo("mute", "Mute a player", "/mute <player> [duration]", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("unmute", "Unmute a player", "/unmute <player>", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("freeze", "Freeze a player", "/freeze <player>", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("unfreeze", "Unfreeze a player", "/unfreeze <player>", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("commandspy", "Toggle command spy", "/commandspy", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("socialspy", "Toggle social spy", "/socialspy", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("give", "Give item to player", "/give <player> <item> [amount]", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("enchant", "Enchant player's item", "/enchant <player> <enchantment> <level>", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("repair", "Repair items", "/repair [all]", "ecore.staff", staff));
        staff.addSubcommand(new CommandInfo("chatslow", "Set chat slow mode", "/chatslow <seconds>", "ecore.staff", staff));
        cmds.add(staff);

        // GameMode Commands
        CommandInfo gamemode = new CommandInfo("gm", "Open GameMode GUI", "/gm", "ecore.gamemode", null);
        cmds.add(gamemode);

        // Report Commands
        CommandInfo report = new CommandInfo("report", "Report a player", "/report <player> <reason>", "ecore.report", null);
        cmds.add(report);

        // WorldEdit Commands
        CommandInfo worldedit = new CommandInfo("worldedit", "WorldEdit building tools", "/wand to get started", "ecore.worldedit.use", null);
        worldedit.addSubcommand(new CommandInfo("wand", "Get WorldEdit selection wand", "/wand", "ecore.worldedit.wand", worldedit));
        worldedit.addSubcommand(new CommandInfo("pos1", "Set selection position 1", "/pos1", "ecore.worldedit.use", worldedit));
        worldedit.addSubcommand(new CommandInfo("pos2", "Set selection position 2", "/pos2", "ecore.worldedit.use", worldedit));
        worldedit.addSubcommand(new CommandInfo("set", "Fill selection with blocks", "/set <block>", "ecore.worldedit.set", worldedit));
        worldedit.addSubcommand(new CommandInfo("replace", "Replace blocks in selection", "/replace <from> <to>", "ecore.worldedit.replace", worldedit));
        worldedit.addSubcommand(new CommandInfo("copy", "Copy selection to clipboard", "/copy", "ecore.worldedit.copy", worldedit));
        worldedit.addSubcommand(new CommandInfo("paste", "Paste clipboard at your location", "/paste", "ecore.worldedit.paste", worldedit));
        worldedit.addSubcommand(new CommandInfo("cut", "Cut selection to clipboard", "/cut", "ecore.worldedit.cut", worldedit));
        worldedit.addSubcommand(new CommandInfo("undo", "Undo last WorldEdit operation", "/undo", "ecore.worldedit.undo", worldedit));
        worldedit.addSubcommand(new CommandInfo("redo", "Redo last undone operation", "/redo", "ecore.worldedit.redo", worldedit));
        worldedit.addSubcommand(new CommandInfo("schematic", "Save/load schematics", "/schematic <save|load|list|delete> [name]", "ecore.worldedit.schematic", worldedit));
        worldedit.addSubcommand(new CommandInfo("sphere", "Create a sphere", "/sphere <radius> <block> [hollow]", "ecore.worldedit.sphere", worldedit));
        worldedit.addSubcommand(new CommandInfo("cylinder", "Create a cylinder", "/cylinder <radius> <height> <block> [hollow]", "ecore.worldedit.cylinder", worldedit));
        cmds.add(worldedit);

        // Region Commands
        CommandInfo region = new CommandInfo("region", "Region management", "/region <create|delete|list|info|flag|flags|addowner|removeowner|addmember|removemember>", "ecore.region", null);
        region.addDetail("Create a region: /region create <name>");
        region.addDetail("Set flags: /region flag <name> <flag> <value>");
        cmds.add(region);

        // Chunks Commands
        CommandInfo chunks = new CommandInfo("chunks", "Chunk pregeneration", "/chunks <generate|cancel|status> [radius]", "ecore.chunks.generate", null);
        cmds.add(chunks);

        // Staff Mode Commands
        CommandInfo staffmode = new CommandInfo("staffmode", "Toggle staff mode", "/staffmode", "ecore.staffmode", null);
        staffmode.addAlias("sm");
        cmds.add(staffmode);

        // World Commands
        CommandInfo world = new CommandInfo("multiverse", "World management", "/mv <create|load|unload|delete|list|tp|spawn|setspawn|info|reload> [args]", "ecore.world.create", null);
        world.addAlias("mv");
        cmds.add(world);

        // Portal Commands
        CommandInfo portal = new CommandInfo("portal", "Portal management", "/portal <create|delete|list|info|setdest|wand> [args]", "ecore.portal.create", null);
        cmds.add(portal);

        // Block Log Commands
        CommandInfo blocklog = new CommandInfo("blocklog", "Block logging and rollback", "/blocklog [lookup|rollback|restore|inspect|inventory|purge|reload] [args]", "ecore.blocklog.use", null);
        blocklog.addAlias("bl");
        blocklog.addAlias("co");
        cmds.add(blocklog);

        // Friend Commands
        CommandInfo friend = new CommandInfo("friend", "Friend system", "/friend [add|remove|list|accept|deny|requests]", "ecore.friend", null);
        friend.addAlias("friends");
        cmds.add(friend);

        // Party Commands
        CommandInfo party = new CommandInfo("party", "Party system", "/party [create|invite|accept|leave|kick|list|chat] [args]", "ecore.party", null);
        party.addAlias("p");
        cmds.add(party);

        // Scoreboard Commands
        CommandInfo scoreboard = new CommandInfo("scoreboard", "Scoreboard management", "/scoreboard [toggle|reload|tablist reload]", "ecore.scoreboard.use", null);
        scoreboard.addAlias("sb");
        cmds.add(scoreboard);

        // Jobs Commands
        CommandInfo jobs = new CommandInfo("jobs", "Jobs system", "/jobs [join|leave|info|top|list] [args]", "ecore.jobs", null);
        cmds.add(jobs);

        // Quest Commands
        CommandInfo quest = new CommandInfo("quest", "Quests system", "/quest [list|start|active|completed|info] [args]", "ecore.quests", null);
        quest.addAlias("quests");
        cmds.add(quest);

        // Chat Channel Commands
        CommandInfo channel = new CommandInfo("channel", "Chat channels system", "/channel [join|leave|list|current|mute|unmute] [args]", "ecore.channel", null);
        channel.addAlias("ch");
        cmds.add(channel);

        // Vault Commands
        CommandInfo vault = new CommandInfo("vault", "Player vault system", "/vault [open|create|rename|trust|untrust|list] [args]", "ecore.vault", null);
        vault.addAlias("pv");
        cmds.add(vault);

        // Title Commands
        CommandInfo title = new CommandInfo("title", "Send title to a player", "/title <player> <title> [subtitle] [fadeIn] [stay] [fadeOut]", "ecore.title", null);
        title.addSubcommand(new CommandInfo("titleall", "Broadcast title to all players", "/titleall <title> [subtitle] [fadeIn] [stay] [fadeOut]", "ecore.title.all", title));
        title.addSubcommand(new CommandInfo("actionbar", "Send action bar message", "/actionbar <player> <message>", "ecore.actionbar", title));
        title.addSubcommand(new CommandInfo("actionbarall", "Broadcast action bar message", "/actionbarall <message>", "ecore.actionbar.all", title));
        title.addSubcommand(new CommandInfo("cleartitle", "Clear title for a player", "/cleartitle [player|all]", "ecore.title.clear", title));
        cmds.add(title);

        // Recipe Commands
        CommandInfo recipe = new CommandInfo("recipe", "Custom recipes management", "/recipe [create|remove|list|reload] [args]", "ecore.recipe", null);
        cmds.add(recipe);

        // Enchantment Commands
        CommandInfo enchantment = new CommandInfo("customenchant", "Custom enchantments", "/customenchant [apply|remove|list|info] [args]", "ecore.enchant", null);
        cmds.add(enchantment);

        // Nickname Commands
        CommandInfo nickname = new CommandInfo("nick", "Nickname management", "/nick [set|reset|color|format|view] [args]", "ecore.nickname", null);
        nickname.addAlias("nickname");
        cmds.add(nickname);

        // Backup Commands
        CommandInfo backup = new CommandInfo("backup", "Backup system", "/backup [create|list|restore|reload] [args]", "ecore.backup.create", null);
        cmds.add(backup);

        // Server Info Commands
        CommandInfo serverinfo = new CommandInfo("serverinfo", "Display server information", "/serverinfo", "ecore.serverinfo", null);
        cmds.add(serverinfo);

        // Ecore Main Command
        CommandInfo ecore = new CommandInfo("ecore", "Main Ecore command", "/ecore [reload|staff|home|tutorial]", "ecore.staff", null);
        ecore.addSubcommand(new CommandInfo("tutorial", "Open tutorial GUI", "/ecore tutorial", null, ecore));
        cmds.add(ecore);

        return cmds;
    }

    /**
     * Data class for command information.
     */
    private static class CommandInfo {
        String name;
        String description;
        String usage;
        String permission;
        CommandInfo parent;
        List<CommandInfo> subcommands;
        List<String> aliases;
        List<String> details;

        CommandInfo(String name, String description, String usage, String permission, CommandInfo parent) {
            this.name = name;
            this.description = description;
            this.usage = usage;
            this.permission = permission;
            this.parent = parent;
            this.subcommands = new ArrayList<>();
            this.aliases = new ArrayList<>();
            this.details = new ArrayList<>();
        }

        void addSubcommand(CommandInfo sub) {
            subcommands.add(sub);
        }

        void addAlias(String alias) {
            aliases.add(alias);
        }

        void addDetail(String detail) {
            details.add(detail);
        }
    }

    /**
     * Data class for tracking player GUI state.
     */
    private static class TutorialState {
        String currentParent;
        int currentPage;
        CommandInfo detailCommand;

        TutorialState(String currentParent, int currentPage, CommandInfo detailCommand) {
            this.currentParent = currentParent;
            this.currentPage = currentPage;
            this.detailCommand = detailCommand;
        }
    }
}

