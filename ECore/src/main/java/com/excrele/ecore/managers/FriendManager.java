package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages friend lists and friend requests for players.
 */
public class FriendManager {
    private final Ecore plugin;
    private File friendsFile;
    private FileConfiguration friendsConfig;
    private final Map<UUID, Set<UUID>> friends; // Player UUID -> Set of friend UUIDs
    private final Map<UUID, Set<UUID>> pendingRequests; // Target UUID -> Set of requester UUIDs

    public FriendManager(Ecore plugin) {
        this.plugin = plugin;
        this.friends = new HashMap<>();
        this.pendingRequests = new HashMap<>();
        initializeFriendsConfig();
        loadFriends();
    }

    private void initializeFriendsConfig() {
        friendsFile = new File(plugin.getDataFolder(), "friends.yml");
        if (!friendsFile.exists()) {
            try {
                friendsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create friends.yml", e);
            }
        }
        friendsConfig = YamlConfiguration.loadConfiguration(friendsFile);
    }

    private void loadFriends() {
        if (friendsConfig.getConfigurationSection("friends") == null) return;

        for (String uuidStr : friendsConfig.getConfigurationSection("friends").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<String> friendList = friendsConfig.getStringList("friends." + uuidStr);
            Set<UUID> friendSet = new HashSet<>();
            
            for (String friendUuidStr : friendList) {
                try {
                    friendSet.add(UUID.fromString(friendUuidStr));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid friend UUID: " + friendUuidStr);
                }
            }
            
            friends.put(uuid, friendSet);
        }
    }

    private void saveFriends() {
        try {
            friendsConfig.set("friends", null);
            for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
                List<String> friendList = new ArrayList<>();
                for (UUID friendUuid : entry.getValue()) {
                    friendList.add(friendUuid.toString());
                }
                friendsConfig.set("friends." + entry.getKey().toString(), friendList);
            }
            friendsConfig.save(friendsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save friends.yml", e);
        }
    }

    /**
     * Sends a friend request from one player to another.
     */
    public boolean sendFriendRequest(Player sender, Player target) {
        UUID senderUuid = sender.getUniqueId();
        UUID targetUuid = target.getUniqueId();

        if (senderUuid.equals(targetUuid)) {
            return false;
        }

        if (areFriends(senderUuid, targetUuid)) {
            return false;
        }

        if (hasPendingRequest(senderUuid, targetUuid)) {
            return false;
        }

        pendingRequests.computeIfAbsent(targetUuid, k -> new HashSet<>()).add(senderUuid);
        
        sender.sendMessage("§aFriend request sent to " + target.getName() + "!");
        target.sendMessage("§a" + sender.getName() + " wants to be your friend! Use /friend accept " + sender.getName());
        
        return true;
    }

    /**
     * Accepts a friend request.
     */
    public boolean acceptFriendRequest(Player accepter, Player requester) {
        UUID accepterUuid = accepter.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        if (!hasPendingRequest(requesterUuid, accepterUuid)) {
            return false;
        }

        pendingRequests.get(accepterUuid).remove(requesterUuid);
        if (pendingRequests.get(accepterUuid).isEmpty()) {
            pendingRequests.remove(accepterUuid);
        }

        addFriend(accepterUuid, requesterUuid);
        addFriend(requesterUuid, accepterUuid);

        accepter.sendMessage("§aYou are now friends with " + requester.getName() + "!");
        requester.sendMessage("§a" + accepter.getName() + " accepted your friend request!");

        return true;
    }

    /**
     * Denies a friend request.
     */
    public boolean denyFriendRequest(Player denier, Player requester) {
        UUID denierUuid = denier.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();

        if (!hasPendingRequest(requesterUuid, denierUuid)) {
            return false;
        }

        pendingRequests.get(denierUuid).remove(requesterUuid);
        if (pendingRequests.get(denierUuid).isEmpty()) {
            pendingRequests.remove(denierUuid);
        }

        denier.sendMessage("§cFriend request from " + requester.getName() + " denied.");
        requester.sendMessage("§c" + denier.getName() + " denied your friend request.");

        return true;
    }

    /**
     * Removes a friend.
     */
    public boolean removeFriend(Player player, Player friend) {
        UUID playerUuid = player.getUniqueId();
        UUID friendUuid = friend.getUniqueId();

        if (!areFriends(playerUuid, friendUuid)) {
            return false;
        }

        friends.get(playerUuid).remove(friendUuid);
        friends.get(friendUuid).remove(playerUuid);

        if (friends.get(playerUuid).isEmpty()) {
            friends.remove(playerUuid);
        }
        if (friends.get(friendUuid).isEmpty()) {
            friends.remove(friendUuid);
        }

        saveFriends();

        player.sendMessage("§cYou are no longer friends with " + friend.getName() + ".");
        friend.sendMessage("§c" + player.getName() + " removed you from their friend list.");

        return true;
    }

    /**
     * Adds a friend (internal method).
     */
    private void addFriend(UUID uuid1, UUID uuid2) {
        friends.computeIfAbsent(uuid1, k -> new HashSet<>()).add(uuid2);
        saveFriends();
    }

    /**
     * Checks if two players are friends.
     */
    public boolean areFriends(UUID uuid1, UUID uuid2) {
        return friends.getOrDefault(uuid1, Collections.emptySet()).contains(uuid2);
    }

    /**
     * Checks if there's a pending request.
     */
    public boolean hasPendingRequest(UUID requesterUuid, UUID targetUuid) {
        return pendingRequests.getOrDefault(targetUuid, Collections.emptySet()).contains(requesterUuid);
    }

    /**
     * Gets the friend list for a player.
     */
    public List<UUID> getFriends(UUID playerUuid) {
        return new ArrayList<>(friends.getOrDefault(playerUuid, Collections.emptySet()));
    }

    /**
     * Gets pending friend requests for a player.
     */
    public List<UUID> getPendingRequests(UUID playerUuid) {
        return new ArrayList<>(pendingRequests.getOrDefault(playerUuid, Collections.emptySet()));
    }

    /**
     * Gets the number of friends for a player.
     */
    public int getFriendCount(UUID playerUuid) {
        return friends.getOrDefault(playerUuid, Collections.emptySet()).size();
    }

    /**
     * Checks if a player is online and is a friend.
     */
    public boolean isFriendOnline(UUID playerUuid, UUID friendUuid) {
        if (!areFriends(playerUuid, friendUuid)) return false;
        Player friend = Bukkit.getPlayer(friendUuid);
        return friend != null && friend.isOnline();
    }
}

