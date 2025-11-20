package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI manager for region system.
 * Provides easy-to-use interfaces for managing regions, flags, permissions, and rent/sell.
 */
public class RegionGUIManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, String> openRegions; // Player UUID -> Region name
    private final Map<UUID, String> guiType; // Player UUID -> GUI type (main, flags, permissions, rent)

    public RegionGUIManager(Ecore plugin) {
        this.plugin = plugin;
        this.openRegions = new HashMap<>();
        this.guiType = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the main region list GUI.
     */
    public void openRegionListGUI(Player player, World world) {
        Collection<Region> regions = plugin.getRegionManager().getRegions(world);
        
        int size = 54; // 6 rows
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Regions - " + world.getName());

        int slot = 0;
        for (Region region : regions) {
            if (slot >= 45) break; // Only show first 45 regions
            
            ItemStack regionItem = createRegionItem(region, player);
            gui.setItem(slot, regionItem);
            slot++;
        }

        // Close button
        ItemStack closeItem = createMenuItem(Material.BARRIER, ChatColor.RED + "Close", 
                Collections.singletonList(ChatColor.GRAY + "Click to close"));
        gui.setItem(49, closeItem);

        player.openInventory(gui);
        guiType.put(player.getUniqueId(), "list");
    }

    /**
     * Opens the region info GUI for a specific region.
     */
    public void openRegionInfoGUI(Player player, Region region) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Region: " + region.getName());

        // Region info item
        ItemStack infoItem = createRegionItem(region, player);
        gui.setItem(4, infoItem);

        // Flags button
        ItemStack flagsItem = createMenuItem(Material.REDSTONE_TORCH, ChatColor.YELLOW + "Manage Flags",
                Arrays.asList(ChatColor.GRAY + "Click to manage region flags"));
        gui.setItem(19, flagsItem);

        // Permissions button
        ItemStack permissionsItem = createMenuItem(Material.BOOK, ChatColor.YELLOW + "Manage Permissions",
                Arrays.asList(ChatColor.GRAY + "Click to manage owners and members"));
        gui.setItem(21, permissionsItem);

        // Rent/Sell button
        ItemStack rentSellItem = createMenuItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Rent/Sell",
                Arrays.asList(ChatColor.GRAY + "Click to manage rent/sell settings"));
        gui.setItem(23, rentSellItem);

        // Visualize button
        ItemStack visualizeItem = createMenuItem(Material.ENDER_EYE, ChatColor.YELLOW + "Visualize Region",
                Arrays.asList(ChatColor.GRAY + "Click to show region borders with particles"));
        gui.setItem(25, visualizeItem);

        // Back button
        ItemStack backItem = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to region list"));
        gui.setItem(45, backItem);

        // Close button
        ItemStack closeItem = createMenuItem(Material.BARRIER, ChatColor.RED + "Close",
                Collections.singletonList(ChatColor.GRAY + "Click to close"));
        gui.setItem(49, closeItem);

        player.openInventory(gui);
        openRegions.put(player.getUniqueId(), region.getName());
        guiType.put(player.getUniqueId(), "info");
    }

    /**
     * Opens the flags management GUI.
     */
    public void openFlagsGUI(Player player, Region region) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Flags: " + region.getName());

        int slot = 9;
        for (RegionFlag flag : RegionFlag.values()) {
            if (slot >= 45) break;
            
            Boolean value = region.getFlag(flag);
            boolean enabled = value != null && value;
            
            Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
            String name = (enabled ? ChatColor.GREEN : ChatColor.RED) + flag.getDescription();
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "Current: " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
                    ChatColor.YELLOW + "Click to toggle"
            );
            
            ItemStack flagItem = createMenuItem(material, name, lore);
            gui.setItem(slot, flagItem);
            slot++;
        }

        // Back button
        ItemStack backItem = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to region info"));
        gui.setItem(45, backItem);

        player.openInventory(gui);
        openRegions.put(player.getUniqueId(), region.getName());
        guiType.put(player.getUniqueId(), "flags");
    }

    /**
     * Opens the permissions management GUI.
     */
    public void openPermissionsGUI(Player player, Region region) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Permissions: " + region.getName());

        // Owners section
        ItemStack ownersItem = createMenuItem(Material.GOLDEN_HELMET, ChatColor.GOLD + "Owners",
                Collections.singletonList(ChatColor.GRAY + "Region owners"));
        gui.setItem(10, ownersItem);

        int ownerSlot = 19;
        for (UUID ownerUuid : region.getOwners()) {
            if (ownerSlot >= 28) break;
            String ownerName = Bukkit.getOfflinePlayer(ownerUuid).getName();
            if (ownerName == null) ownerName = "Unknown";
            
            ItemStack ownerItem = createMenuItem(Material.PLAYER_HEAD, ChatColor.YELLOW + ownerName,
                    Arrays.asList(ChatColor.GRAY + "Owner", ChatColor.RED + "Right-click to remove"));
            gui.setItem(ownerSlot, ownerItem);
            ownerSlot++;
        }

        // Members section
        ItemStack membersItem = createMenuItem(Material.IRON_HELMET, ChatColor.AQUA + "Members",
                Collections.singletonList(ChatColor.GRAY + "Region members"));
        gui.setItem(12, membersItem);

        int memberSlot = 28;
        for (UUID memberUuid : region.getMembers()) {
            if (memberSlot >= 37) break;
            String memberName = Bukkit.getOfflinePlayer(memberUuid).getName();
            if (memberName == null) memberName = "Unknown";
            
            ItemStack memberItem = createMenuItem(Material.PLAYER_HEAD, ChatColor.YELLOW + memberName,
                    Arrays.asList(ChatColor.GRAY + "Member", ChatColor.RED + "Right-click to remove"));
            gui.setItem(memberSlot, memberItem);
            memberSlot++;
        }

        // Add owner button
        ItemStack addOwnerItem = createMenuItem(Material.EMERALD, ChatColor.GREEN + "Add Owner",
                Collections.singletonList(ChatColor.GRAY + "Click to add an owner"));
        gui.setItem(37, addOwnerItem);

        // Add member button
        ItemStack addMemberItem = createMenuItem(Material.EMERALD, ChatColor.GREEN + "Add Member",
                Collections.singletonList(ChatColor.GRAY + "Click to add a member"));
        gui.setItem(39, addMemberItem);

        // Back button
        ItemStack backItem = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to region info"));
        gui.setItem(45, backItem);

        player.openInventory(gui);
        openRegions.put(player.getUniqueId(), region.getName());
        guiType.put(player.getUniqueId(), "permissions");
    }

    /**
     * Opens the rent/sell management GUI.
     */
    public void openRentSellGUI(Player player, Region region) {
        int size = 54;
        Inventory gui = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Rent/Sell: " + region.getName());

        // For Sale toggle
        Material saleMaterial = region.isForSale() ? Material.LIME_DYE : Material.GRAY_DYE;
        String saleName = (region.isForSale() ? ChatColor.GREEN : ChatColor.RED) + "For Sale";
        List<String> saleLore = Arrays.asList(
                ChatColor.GRAY + "Current: " + (region.isForSale() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                ChatColor.GRAY + "Price: " + ChatColor.YELLOW + formatMoney(region.getSalePrice()),
                ChatColor.YELLOW + "Click to toggle"
        );
        ItemStack saleItem = createMenuItem(saleMaterial, saleName, saleLore);
        gui.setItem(10, saleItem);

        // Set sale price
        ItemStack setPriceItem = createMenuItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Set Sale Price",
                Arrays.asList(ChatColor.GRAY + "Current: " + formatMoney(region.getSalePrice()),
                        ChatColor.YELLOW + "Use /region sellprice <name> <price>"));
        gui.setItem(12, setPriceItem);

        // For Rent toggle
        Material rentMaterial = region.isForRent() ? Material.LIME_DYE : Material.GRAY_DYE;
        String rentName = (region.isForRent() ? ChatColor.GREEN : ChatColor.RED) + "For Rent";
        List<String> rentLore = Arrays.asList(
                ChatColor.GRAY + "Current: " + (region.isForRent() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                ChatColor.GRAY + "Price: " + ChatColor.YELLOW + formatMoney(region.getRentPrice()),
                ChatColor.GRAY + "Duration: " + ChatColor.YELLOW + formatDuration(region.getRentDuration()),
                ChatColor.YELLOW + "Click to toggle"
        );
        ItemStack rentItem = createMenuItem(rentMaterial, rentName, rentLore);
        gui.setItem(14, rentItem);

        // Set rent price
        ItemStack setRentPriceItem = createMenuItem(Material.GOLD_NUGGET, ChatColor.YELLOW + "Set Rent Price",
                Arrays.asList(ChatColor.GRAY + "Current: " + formatMoney(region.getRentPrice()),
                        ChatColor.YELLOW + "Use /region rentprice <name> <price>"));
        gui.setItem(16, setRentPriceItem);

        // Rent status
        if (region.isRented()) {
            String renterName = Bukkit.getOfflinePlayer(region.getRenter()).getName();
            if (renterName == null) renterName = "Unknown";
            long timeLeft = region.getRentExpires() - System.currentTimeMillis();
            
            ItemStack rentStatusItem = createMenuItem(Material.CLOCK, ChatColor.YELLOW + "Rent Status",
                    Arrays.asList(
                            ChatColor.GRAY + "Rented by: " + ChatColor.YELLOW + renterName,
                            ChatColor.GRAY + "Time left: " + ChatColor.YELLOW + formatDuration(timeLeft)
                    ));
            gui.setItem(22, rentStatusItem);
        }

        // Back button
        ItemStack backItem = createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back",
                Collections.singletonList(ChatColor.GRAY + "Return to region info"));
        gui.setItem(45, backItem);

        player.openInventory(gui);
        openRegions.put(player.getUniqueId(), region.getName());
        guiType.put(player.getUniqueId(), "rentsell");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String type = guiType.get(player.getUniqueId());
        if (type == null) return;
        
        event.setCancelled(true);
        
        String regionName = openRegions.get(player.getUniqueId());
        if (regionName == null) return;
        
        Region region = plugin.getRegionManager().getRegion(player.getWorld(), regionName);
        if (region == null) {
            player.closeInventory();
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        switch (type) {
            case "list":
                handleListClick(player, clicked, event.getSlot());
                break;
            case "info":
                handleInfoClick(player, region, clicked, event.getSlot());
                break;
            case "flags":
                handleFlagsClick(player, region, clicked, event.getSlot());
                break;
            case "permissions":
                handlePermissionsClick(player, region, clicked, event.getSlot());
                break;
            case "rentsell":
                handleRentSellClick(player, region, clicked, event.getSlot());
                break;
        }
    }

    private void handleListClick(Player player, ItemStack clicked, int slot) {
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // Open region info if clicked on a region item
        if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Region region = plugin.getRegionManager().getRegion(player.getWorld(), displayName);
            if (region != null) {
                openRegionInfoGUI(player, region);
            }
        }
    }

    private void handleInfoClick(Player player, Region region, ItemStack clicked, int slot) {
        if (slot == 45) {
            openRegionListGUI(player, player.getWorld());
            return;
        }
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        
        if (slot == 19) { // Flags
            openFlagsGUI(player, region);
        } else if (slot == 21) { // Permissions
            openPermissionsGUI(player, region);
        } else if (slot == 23) { // Rent/Sell
            openRentSellGUI(player, region);
        } else if (slot == 25) { // Visualize
            plugin.getRegionManager().visualizeRegion(player, region);
            player.sendMessage(ChatColor.GREEN + "Region borders visualized!");
        }
    }

    private void handleFlagsClick(Player player, Region region, ItemStack clicked, int slot) {
        if (slot == 45) {
            openRegionInfoGUI(player, region);
            return;
        }
        
        // Toggle flag
        RegionFlag[] flags = RegionFlag.values();
        int flagIndex = slot - 9;
        if (flagIndex >= 0 && flagIndex < flags.length) {
            RegionFlag flag = flags[flagIndex];
            Boolean current = region.getFlag(flag);
            boolean newValue = current == null || !current;
            region.setFlag(flag, newValue);
            plugin.getRegionManager().saveRegions();
            openFlagsGUI(player, region);
            player.sendMessage(ChatColor.GREEN + "Flag " + flag.getDescription() + " set to " + 
                    (newValue ? "enabled" : "disabled"));
        }
    }

    private void handlePermissionsClick(Player player, Region region, ItemStack clicked, int slot) {
        if (slot == 45) {
            openRegionInfoGUI(player, region);
            return;
        }
        
        // Handle add owner/member buttons
        if (slot == 37) {
            player.sendMessage(ChatColor.YELLOW + "Use /region addowner " + region.getName() + " <player>");
            player.closeInventory();
        } else if (slot == 39) {
            player.sendMessage(ChatColor.YELLOW + "Use /region addmember " + region.getName() + " <player>");
            player.closeInventory();
        }
    }

    private void handleRentSellClick(Player player, Region region, ItemStack clicked, int slot) {
        if (slot == 45) {
            openRegionInfoGUI(player, region);
            return;
        }
        
        if (slot == 10) { // Toggle for sale
            region.setForSale(!region.isForSale());
            plugin.getRegionManager().saveRegions();
            openRentSellGUI(player, region);
            player.sendMessage(ChatColor.GREEN + "Region is now " + (region.isForSale() ? "for sale" : "not for sale"));
        } else if (slot == 14) { // Toggle for rent
            region.setForRent(!region.isForRent());
            plugin.getRegionManager().saveRegions();
            openRentSellGUI(player, region);
            player.sendMessage(ChatColor.GREEN + "Region is now " + (region.isForRent() ? "for rent" : "not for rent"));
        }
    }

    private ItemStack createRegionItem(Region region, Player player) {
        Material material = Material.GOLD_BLOCK;
        String name = ChatColor.GOLD + region.getName();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.YELLOW + region.getRegionType());
        lore.add(ChatColor.GRAY + "World: " + ChatColor.YELLOW + region.getWorld().getName());
        lore.add(ChatColor.GRAY + "Volume: " + ChatColor.YELLOW + region.getVolume() + " blocks");
        
        if (region.isForSale()) {
            lore.add(ChatColor.GREEN + "For Sale: " + ChatColor.YELLOW + 
                    formatMoney(region.getSalePrice()));
        }
        if (region.isForRent()) {
            lore.add(ChatColor.AQUA + "For Rent: " + ChatColor.YELLOW + 
                    formatMoney(region.getRentPrice()));
        }
        
        boolean isOwner = region.isOwner(player.getUniqueId());
        boolean isMember = region.isMember(player.getUniqueId());
        if (isOwner) {
            lore.add(ChatColor.GOLD + "You are an owner");
        } else if (isMember) {
            lore.add(ChatColor.AQUA + "You are a member");
        }
        
        lore.add(ChatColor.YELLOW + "Click to view details");
        
        return createMenuItem(material, name, lore);
    }

    private ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) return "0s";
        
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    private String formatMoney(double amount) {
        return String.format("%.2f", amount);
    }
}

