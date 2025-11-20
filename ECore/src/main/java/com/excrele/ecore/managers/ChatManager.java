package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.excrele.ecore.Ecore;

/**
 * Manages all chat-related functionality including:
 * - Chat slow mode
 * - Chat cooldowns
 * - Mute system
 * - Private messaging
 * - Staff/Admin chat
 * - Social spy logging
 * 
 * @author Excrele
 * @version 1.0
 */
public class ChatManager implements Listener {
    private final Ecore plugin;
    private final Map<UUID, UUID> lastMessaged;
    private final Map<UUID, Long> chatCooldowns;
    private final Map<UUID, Long> mutedPlayers; // UUID -> expiration timestamp
    private final Map<UUID, Long> slowModeLastMessage; // UUID -> last message timestamp
    private boolean chatEnabled = true;
    private int slowModeSeconds = 0; // 0 = disabled
    private File mutesFile;
    private FileConfiguration mutesConfig;

    public ChatManager(Ecore plugin) {
        this.plugin = plugin;
        this.lastMessaged = new HashMap<>();
        this.chatCooldowns = new HashMap<>();
        this.mutedPlayers = new HashMap<>();
        this.slowModeLastMessage = new HashMap<>();
        initializeMutesConfig();
        loadMutes();
        // Load slow mode from config
        this.slowModeSeconds = plugin.getConfig().getInt("chat.slow-mode", 0);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeMutesConfig() {
        mutesFile = new File(plugin.getDataFolder(), "mutes.yml");
        if (!mutesFile.exists()) {
            try {
                mutesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create mutes.yml", e);
            }
        }
        mutesConfig = YamlConfiguration.loadConfiguration(mutesFile);
    }

    private void loadMutes() {
        if (mutesConfig.contains("mutes")) {
            org.bukkit.configuration.ConfigurationSection mutesSection = mutesConfig.getConfigurationSection("mutes");
            if (mutesSection != null) {
                for (String uuidStr : mutesSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        long expires = mutesConfig.getLong("mutes." + uuidStr + ".expires", 0);
                        if (expires == 0 || expires > System.currentTimeMillis()) {
                            mutedPlayers.put(uuid, expires);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in mutes.yml: " + uuidStr);
                    }
                }
            }
        }
    }

    private void saveMutes() {
        try {
            mutesConfig.save(mutesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mutes.yml: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if muted
        if (isMuted(player)) {
            long remaining = getMuteRemaining(player);
            if (remaining > 0) {
                event.setCancelled(true);
                player.sendMessage("§cYou are muted! Time remaining: " + formatTime(remaining));
            } else {
                // Mute expired, remove it
                unmutePlayer(player);
            }
            return;
        }

        if (!chatEnabled) {
            if (!player.hasPermission("ecore.chat.bypass")) {
                event.setCancelled(true);
                player.sendMessage("§cChat is currently disabled!");
                return;
            }
        }

        // Check slow mode (staff can bypass)
        if (slowModeSeconds > 0 && !player.hasPermission("ecore.chat.bypass")) {
            UUID uuid = player.getUniqueId();
            Long lastMessageTime = slowModeLastMessage.get(uuid);
            if (lastMessageTime != null) {
                long timeSince = (System.currentTimeMillis() - lastMessageTime) / 1000;
                if (timeSince < slowModeSeconds) {
                    long remaining = slowModeSeconds - timeSince;
                    event.setCancelled(true);
                    player.sendMessage("§cChat slow mode is active! Please wait " + remaining + " more second(s) before chatting again!");
                    return;
                }
            }
            slowModeLastMessage.put(uuid, System.currentTimeMillis());
        }

        // Check cooldown
        if (hasCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            event.setCancelled(true);
            player.sendMessage("§cPlease wait " + (remaining / 1000) + " more seconds before chatting again!");
            return;
        }

        setCooldown(player);
        
        // Handle pending actions
        String pendingAction = plugin.getPendingAction(player.getUniqueId());
        if (pendingAction != null) {
            event.setCancelled(true);
            handlePendingAction(player, event.getMessage(), pendingAction);
            plugin.removePendingAction(player.getUniqueId());
        }
    }
    
    private void handlePendingAction(Player player, String message, String action) {
        if (action.startsWith("ah:bid:")) {
            // Auction house bid
            try {
                int auctionId = Integer.parseInt(action.split(":")[2]);
                if (message.equalsIgnoreCase("cancel")) {
                    player.sendMessage("§cBid cancelled.");
                    return;
                }
                
                double bidAmount = Double.parseDouble(message);
                if (plugin.getAuctionHouseManager().placeBid(player, auctionId, bidAmount)) {
                    player.sendMessage("§aBid placed successfully!");
                } else {
                    player.sendMessage("§cFailed to place bid! Check that the auction exists and your bid is high enough.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid bid amount! Please enter a number or 'cancel'.");
                plugin.registerPendingAction(player, action); // Re-register to try again
            }
        } else if (action.startsWith("home:set")) {
            // Home setting (handled elsewhere, but we cancel chat here)
            // This is handled in HomeGUIManager or similar
        } else if (action.equals("friend:add")) {
            // Friend add
            Player target = Bukkit.getPlayer(message);
            if (target == null) {
                player.sendMessage("§cPlayer not found: " + message);
                plugin.registerPendingAction(player, action);
            } else {
                plugin.getFriendManager().sendFriendRequest(player, target);
            }
        } else if (action.startsWith("party:invite")) {
            // Party invite
            Player target = Bukkit.getPlayer(message);
            if (target == null) {
                player.sendMessage("§cPlayer not found: " + message);
                plugin.registerPendingAction(player, action);
            } else {
                plugin.getPartyManager().invitePlayer(player, target);
            }
        } else if (action.startsWith("party:kick")) {
            // Party kick
            Player target = Bukkit.getPlayer(message);
            if (target == null) {
                player.sendMessage("§cPlayer not found: " + message);
                plugin.registerPendingAction(player, action);
            } else {
                plugin.getPartyManager().kickPlayer(player, target);
            }
        } else if (action.startsWith("blocklog:")) {
            // Block log actions (handled in BlockLogCommand or similar)
            String[] parts = action.split(":");
            if (parts.length >= 3 && parts[2].equals("player")) {
                Player target = Bukkit.getPlayer(message);
                if (target == null) {
                    player.sendMessage("§cPlayer not found: " + message);
                    plugin.registerPendingAction(player, action);
                } else {
                    // Handle based on action type
                    if (parts[1].equals("lookup")) {
                        plugin.getBlockLogGUIManager().openLookupGUI(player, target.getUniqueId(), target.getName(), 3600000L);
                    } else if (parts[1].equals("rollback")) {
                        plugin.getBlockLogManager().rollbackPlayer(target.getUniqueId(), 3600000L, player);
                    } else if (parts[1].equals("inventory")) {
                        plugin.getBlockLogGUIManager().openInventoryRollbackGUI(player, target.getUniqueId(), target.getName(), 3600000L);
                    }
                }
            }
        }
    }

    public void sendPrivateMessage(Player sender, Player recipient, String message) {
        if (recipient == null || !recipient.isOnline()) {
            sender.sendMessage("§cPlayer not found or offline!");
            return;
        }

        lastMessaged.put(sender.getUniqueId(), recipient.getUniqueId());
        lastMessaged.put(recipient.getUniqueId(), sender.getUniqueId());

        String senderFormat = "§7[§6You → " + recipient.getName() + "§7] §f" + message;
        String recipientFormat = "§7[§6" + sender.getName() + " → You§7] §f" + message;

        sender.sendMessage(senderFormat);
        recipient.sendMessage(recipientFormat);

        // Social spy logging
        logPrivateMessageForSpy(sender, recipient, message);

        // Send to Discord if enabled
        plugin.getDiscordManager().sendStaffLogNotification(
            "private-message",
            sender.getName(),
            "sent message to",
            recipient.getName(),
            message
        );
    }

    public void replyToLastMessage(Player player, String message) {
        UUID lastMessagedUuid = lastMessaged.get(player.getUniqueId());
        if (lastMessagedUuid == null) {
            player.sendMessage("§cYou have no one to reply to!");
            return;
        }

        Player target = Bukkit.getPlayer(lastMessagedUuid);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cThat player is no longer online!");
            lastMessaged.remove(player.getUniqueId());
            return;
        }

        sendPrivateMessage(player, target, message);
    }

    public void setChatEnabled(boolean enabled) {
        this.chatEnabled = enabled;
        if (!enabled) {
            Bukkit.broadcastMessage("§cChat has been disabled by an administrator.");
        } else {
            Bukkit.broadcastMessage("§aChat has been enabled.");
        }
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    private boolean hasCooldown(Player player) {
        if (player.hasPermission("ecore.chat.bypass")) {
            return false;
        }
        Long cooldownEnd = chatCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownEnd;
    }

    private long getCooldownRemaining(Player player) {
        Long cooldownEnd = chatCooldowns.get(player.getUniqueId());
        if (cooldownEnd == null) {
            return 0;
        }
        return Math.max(0, cooldownEnd - System.currentTimeMillis());
    }

    private void setCooldown(Player player) {
        int cooldown = plugin.getConfig().getInt("chat.cooldown", 0);
        if (cooldown > 0) {
            chatCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + (cooldown * 1000L));
        }
    }

    // Mute system
    public void mutePlayer(Player player, long durationSeconds) {
        UUID uuid = player.getUniqueId();
        long expires = durationSeconds > 0 ? System.currentTimeMillis() + (durationSeconds * 1000L) : 0;
        mutedPlayers.put(uuid, expires);
        mutesConfig.set("mutes." + uuid.toString() + ".expires", expires);
        mutesConfig.set("mutes." + uuid.toString() + ".name", player.getName());
        saveMutes();
        
        if (durationSeconds > 0) {
            player.sendMessage("§cYou have been muted for " + formatTime(durationSeconds * 1000L) + "!");
        } else {
            player.sendMessage("§cYou have been permanently muted!");
        }
    }

    public void unmutePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        mutedPlayers.remove(uuid);
        mutesConfig.set("mutes." + uuid.toString(), null);
        saveMutes();
        player.sendMessage("§aYou have been unmuted!");
    }

    public boolean isMuted(Player player) {
        UUID uuid = player.getUniqueId();
        Long expires = mutedPlayers.get(uuid);
        if (expires == null) {
            return false;
        }
        if (expires > 0 && expires < System.currentTimeMillis()) {
            mutedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    private long getMuteRemaining(Player player) {
        UUID uuid = player.getUniqueId();
        Long expires = mutedPlayers.get(uuid);
        if (expires == null || expires == 0) {
            return 0;
        }
        return Math.max(0, expires - System.currentTimeMillis());
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes";
        } else {
            return (seconds / 3600) + " hours";
        }
    }

    // Staff chat
    public void sendStaffChat(Player player, String message) {
        String format = ChatColor.translateAlternateColorCodes('&', 
            "&8[&cStaff&8] &7" + player.getName() + "&8: &f" + message);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("ecore.staff")) {
                p.sendMessage(format);
            }
        }
        
        // Log to Discord
        plugin.getDiscordManager().sendStaffLogNotification(
            "staff-chat",
            player.getName(),
            "staff chat",
            "",
            message
        );
    }

    // Admin chat
    public void sendAdminChat(Player player, String message) {
        String format = ChatColor.translateAlternateColorCodes('&', 
            "&8[&4Admin&8] &7" + player.getName() + "&8: &f" + message);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("ecore.admin")) {
                p.sendMessage(format);
            }
        }
        
        // Log to Discord
        plugin.getDiscordManager().sendStaffLogNotification(
            "admin-chat",
            player.getName(),
            "admin chat",
            "",
            message
        );
    }

    // Social spy - log private messages to staff
    public void logPrivateMessageForSpy(Player sender, Player recipient, String message) {
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("ecore.staff") && 
                plugin.getStaffManager().isSocialSpyEnabled(staff) &&
                !staff.equals(sender) && !staff.equals(recipient)) {
                staff.sendMessage(ChatColor.GRAY + "[SocialSpy] " + 
                    ChatColor.YELLOW + sender.getName() + ChatColor.GRAY + " → " + 
                    ChatColor.YELLOW + recipient.getName() + ChatColor.GRAY + ": " + 
                    ChatColor.WHITE + message);
            }
        }
    }

    // Slow mode methods
    public void setSlowMode(int seconds) {
        this.slowModeSeconds = seconds;
        plugin.getConfig().set("chat.slow-mode", seconds);
        plugin.saveConfig();
        
        if (seconds > 0) {
            Bukkit.broadcastMessage("§cChat slow mode has been enabled! You can only chat once every " + seconds + " second(s).");
        } else {
            Bukkit.broadcastMessage("§aChat slow mode has been disabled!");
        }
    }

    public int getSlowMode() {
        return slowModeSeconds;
    }

    public boolean isSlowModeEnabled() {
        return slowModeSeconds > 0;
    }
}

