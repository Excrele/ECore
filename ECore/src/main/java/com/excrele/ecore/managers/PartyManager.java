package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages party/team system for players.
 */
public class PartyManager {
    private final Ecore plugin;
    // Parties are stored in memory for now (can be enhanced to persist)
    private final Map<UUID, Party> parties; // Leader UUID -> Party
    private final Map<UUID, UUID> playerParties; // Player UUID -> Leader UUID

    public PartyManager(Ecore plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.playerParties = new HashMap<>();
        initializePartiesConfig();
        loadParties();
    }

    private void initializePartiesConfig() {
        // Parties are stored in memory for now
        // Could be enhanced to persist to file
    }

    private void loadParties() {
        // Parties are stored in memory only for now
        // Could be enhanced to persist parties
    }

    /**
     * Creates a new party.
     */
    public Party createParty(Player leader) {
        UUID leaderUuid = leader.getUniqueId();
        
        if (playerParties.containsKey(leaderUuid)) {
            return null; // Player is already in a party
        }

        Party party = new Party(leaderUuid, leader.getName());
        parties.put(leaderUuid, party);
        playerParties.put(leaderUuid, leaderUuid);

        leader.sendMessage("§aParty created! Use /party invite <player> to invite members.");
        return party;
    }

    /**
     * Invites a player to a party.
     */
    public boolean invitePlayer(Player leader, Player target) {
        UUID leaderUuid = leader.getUniqueId();
        Party party = parties.get(leaderUuid);

        if (party == null) {
            return false;
        }

        if (!party.isLeader(leaderUuid)) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();

        if (playerParties.containsKey(targetUuid)) {
            return false;
        }

        if (party.getMembers().size() >= party.getMaxSize()) {
            return false;
        }

        party.addInvite(targetUuid);
        
        leader.sendMessage("§aInvited " + target.getName() + " to the party!");
        target.sendMessage("§a" + leader.getName() + " invited you to their party! Use /party accept to join.");

        return true;
    }

    /**
     * Accepts a party invite.
     */
    public boolean acceptInvite(Player player, Player leader) {
        UUID playerUuid = player.getUniqueId();
        UUID leaderUuid = leader.getUniqueId();
        Party party = parties.get(leaderUuid);

        if (party == null) {
            return false;
        }

        if (!party.hasInvite(playerUuid)) {
            return false;
        }

        if (playerParties.containsKey(playerUuid)) {
            return false;
        }

        party.removeInvite(playerUuid);
        party.addMember(playerUuid, player.getName());
        playerParties.put(playerUuid, leaderUuid);

        party.broadcast("§a" + player.getName() + " joined the party!");
        player.sendMessage("§aYou joined " + leader.getName() + "'s party!");

        return true;
    }

    /**
     * Leaves a party.
     */
    public boolean leaveParty(Player player) {
        UUID playerUuid = player.getUniqueId();
        UUID leaderUuid = playerParties.get(playerUuid);

        if (leaderUuid == null) {
            return false;
        }

        Party party = parties.get(leaderUuid);
        if (party == null) {
            playerParties.remove(playerUuid);
            return false;
        }

        if (party.isLeader(playerUuid)) {
            // Leader leaving - disband party or transfer leadership
            disbandParty(leaderUuid);
            player.sendMessage("§cParty disbanded.");
        } else {
            party.removeMember(playerUuid);
            playerParties.remove(playerUuid);
            party.broadcast("§c" + player.getName() + " left the party.");
            player.sendMessage("§cYou left the party.");
        }

        return true;
    }

    /**
     * Kicks a player from the party.
     */
    public boolean kickPlayer(Player leader, Player target) {
        UUID leaderUuid = leader.getUniqueId();
        Party party = parties.get(leaderUuid);

        if (party == null || !party.isLeader(leaderUuid)) {
            return false;
        }

        UUID targetUuid = target.getUniqueId();
        if (!party.isMember(targetUuid)) {
            return false;
        }

        party.removeMember(targetUuid);
        playerParties.remove(targetUuid);

        party.broadcast("§c" + target.getName() + " was kicked from the party.");
        target.sendMessage("§cYou were kicked from " + leader.getName() + "'s party.");

        return true;
    }

    /**
     * Disbands a party.
     */
    public void disbandParty(UUID leaderUuid) {
        Party party = parties.remove(leaderUuid);
        if (party != null) {
            party.broadcast("§cParty disbanded.");
            for (UUID memberUuid : party.getMembers().keySet()) {
                playerParties.remove(memberUuid);
            }
        }
    }

    /**
     * Gets the party a player is in.
     */
    public Party getParty(UUID playerUuid) {
        UUID leaderUuid = playerParties.get(playerUuid);
        return leaderUuid != null ? parties.get(leaderUuid) : null;
    }

    /**
     * Checks if a player is in a party.
     */
    public boolean isInParty(UUID playerUuid) {
        return playerParties.containsKey(playerUuid);
    }

    /**
     * Sends a message to party chat.
     */
    public void sendPartyMessage(Player sender, String message) {
        UUID senderUuid = sender.getUniqueId();
        Party party = getParty(senderUuid);

        if (party == null) {
            sender.sendMessage("§cYou are not in a party!");
            return;
        }

        party.broadcast("§d[Party] §7" + sender.getName() + ": §f" + message);
    }

    /**
     * Represents a party.
     */
    public static class Party {
        private final UUID leaderUuid;
        private final String leaderName;
        private final Map<UUID, String> members; // UUID -> Name
        private final Set<UUID> invites; // Invited UUIDs
        private int maxSize;

        public Party(UUID leaderUuid, String leaderName) {
            this.leaderUuid = leaderUuid;
            this.leaderName = leaderName;
            this.members = new HashMap<>();
            this.invites = new HashSet<>();
            this.maxSize = 10; // Default max size
            this.members.put(leaderUuid, leaderName);
        }

        public UUID getLeaderUuid() {
            return leaderUuid;
        }

        public String getLeaderName() {
            return leaderName;
        }

        public Map<UUID, String> getMembers() {
            return members;
        }

        public Set<UUID> getInvites() {
            return invites;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public boolean isLeader(UUID uuid) {
            return leaderUuid.equals(uuid);
        }

        public boolean isMember(UUID uuid) {
            return members.containsKey(uuid);
        }

        public boolean hasInvite(UUID uuid) {
            return invites.contains(uuid);
        }

        public void addMember(UUID uuid, String name) {
            members.put(uuid, name);
        }

        public void removeMember(UUID uuid) {
            members.remove(uuid);
        }

        public void addInvite(UUID uuid) {
            invites.add(uuid);
        }

        public void removeInvite(UUID uuid) {
            invites.remove(uuid);
        }

        public void broadcast(String message) {
            for (UUID memberUuid : members.keySet()) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    member.sendMessage(message);
                }
            }
        }
    }
}

