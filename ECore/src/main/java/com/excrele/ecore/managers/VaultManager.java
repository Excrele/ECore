package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages player vaults (extra storage).
 */
public class VaultManager {
    private final Ecore plugin;
    private File vaultsFile;
    private FileConfiguration vaultsConfig;
    private final Map<UUID, Map<Integer, ItemStack[]>> playerVaults; // Player UUID -> Vault Number -> Items

    public VaultManager(Ecore plugin) {
        this.plugin = plugin;
        this.playerVaults = new HashMap<>();
        initializeVaultsConfig();
        loadVaults();
    }

    private void initializeVaultsConfig() {
        vaultsFile = new File(plugin.getDataFolder(), "vaults.yml");
        if (!vaultsFile.exists()) {
            try {
                vaultsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create vaults.yml", e);
            }
        }
        vaultsConfig = YamlConfiguration.loadConfiguration(vaultsFile);
    }

    private void loadVaults() {
        if (vaultsConfig.getConfigurationSection("vaults") == null) return;

        for (String uuidStr : vaultsConfig.getConfigurationSection("vaults").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Map<Integer, ItemStack[]> vaults = new HashMap<>();
            
            String path = "vaults." + uuidStr;
            if (vaultsConfig.getConfigurationSection(path) != null) {
                for (String vaultNumStr : vaultsConfig.getConfigurationSection(path).getKeys(false)) {
                    try {
                        int vaultNum = Integer.parseInt(vaultNumStr);
                        List<?> itemsList = vaultsConfig.getList(path + "." + vaultNumStr);
                        if (itemsList != null) {
                            ItemStack[] items = new ItemStack[54]; // 6 rows = 54 slots
                            for (int i = 0; i < itemsList.size() && i < 54; i++) {
                                Object itemObj = itemsList.get(i);
                                if (itemObj instanceof ItemStack) {
                                    items[i] = (ItemStack) itemObj;
                                }
                            }
                            vaults.put(vaultNum, items);
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid vault number: " + vaultNumStr);
                    }
                }
            }
            
            playerVaults.put(uuid, vaults);
        }
    }

    private void saveVaults() {
        try {
            vaultsConfig.set("vaults", null);
            for (Map.Entry<UUID, Map<Integer, ItemStack[]>> playerEntry : playerVaults.entrySet()) {
                String uuidPath = "vaults." + playerEntry.getKey().toString();
                for (Map.Entry<Integer, ItemStack[]> vaultEntry : playerEntry.getValue().entrySet()) {
                    List<ItemStack> itemsList = new ArrayList<>();
                    for (ItemStack item : vaultEntry.getValue()) {
                        itemsList.add(item);
                    }
                    vaultsConfig.set(uuidPath + "." + vaultEntry.getKey(), itemsList);
                }
            }
            vaultsConfig.save(vaultsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save vaults.yml", e);
        }
    }

    /**
     * Gets the maximum number of vaults a player can have.
     */
    public int getMaxVaults(Player player) {
        int defaultMax = plugin.getConfigManager().getConfig().getInt("vaults.max-vaults", 1);
        
        // Check for permission-based vault limits
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("ecore.vault." + i)) {
                return i;
            }
        }
        
        return defaultMax;
    }

    /**
     * Gets all vault numbers for a player.
     */
    public List<Integer> getPlayerVaults(Player player) {
        Map<Integer, ItemStack[]> vaults = playerVaults.getOrDefault(player.getUniqueId(), new HashMap<>());
        return new ArrayList<>(vaults.keySet());
    }

    /**
     * Gets the items in a specific vault.
     */
    public ItemStack[] getVaultItems(Player player, int vaultNumber) {
        Map<Integer, ItemStack[]> vaults = playerVaults.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        return vaults.computeIfAbsent(vaultNumber, k -> new ItemStack[54]);
    }

    /**
     * Sets the items in a specific vault.
     */
    public void setVaultItems(Player player, int vaultNumber, ItemStack[] items) {
        Map<Integer, ItemStack[]> vaults = playerVaults.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        vaults.put(vaultNumber, items);
        saveVaults();
    }

    /**
     * Creates a new vault for a player.
     */
    public boolean createVault(Player player, int vaultNumber) {
        int maxVaults = getMaxVaults(player);
        Map<Integer, ItemStack[]> vaults = playerVaults.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        
        if (vaults.size() >= maxVaults) {
            return false; // Already at max vaults
        }
        
        if (vaults.containsKey(vaultNumber)) {
            return false; // Vault already exists
        }
        
        vaults.put(vaultNumber, new ItemStack[54]);
        saveVaults();
        return true;
    }

    /**
     * Checks if a player has access to a vault.
     */
    public boolean hasVault(Player player, int vaultNumber) {
        Map<Integer, ItemStack[]> vaults = playerVaults.getOrDefault(player.getUniqueId(), new HashMap<>());
        return vaults.containsKey(vaultNumber);
    }

    /**
     * Gets the number of vaults a player has.
     */
    public int getVaultCount(Player player) {
        Map<Integer, ItemStack[]> vaults = playerVaults.getOrDefault(player.getUniqueId(), new HashMap<>());
        return vaults.size();
    }

    /**
     * Gets vault name if set, otherwise returns default name.
     */
    public String getVaultName(Player player, int vaultNumber) {
        String path = "vault-names." + player.getUniqueId().toString() + "." + vaultNumber;
        String name = vaultsConfig.getString(path);
        return name != null ? name : "Vault #" + vaultNumber;
    }

    /**
     * Sets vault name.
     */
    public void setVaultName(Player player, int vaultNumber, String name) {
        String path = "vault-names." + player.getUniqueId().toString() + "." + vaultNumber;
        vaultsConfig.set(path, name);
        try {
            vaultsConfig.save(vaultsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save vault name", e);
        }
    }

    /**
     * Gets trusted players for a vault (friends who can access).
     */
    public List<UUID> getTrustedPlayers(Player player, int vaultNumber) {
        String path = "vault-trust." + player.getUniqueId().toString() + "." + vaultNumber;
        List<String> trustedUuids = vaultsConfig.getStringList(path);
        List<UUID> result = new ArrayList<>();
        for (String uuidStr : trustedUuids) {
            try {
                result.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid trusted player UUID: " + uuidStr);
            }
        }
        return result;
    }

    /**
     * Adds a trusted player to a vault.
     */
    public void addTrustedPlayer(Player owner, int vaultNumber, UUID trustedUuid) {
        String path = "vault-trust." + owner.getUniqueId().toString() + "." + vaultNumber;
        List<String> trustedUuids = new ArrayList<>(vaultsConfig.getStringList(path));
        String uuidStr = trustedUuid.toString();
        if (!trustedUuids.contains(uuidStr)) {
            trustedUuids.add(uuidStr);
            vaultsConfig.set(path, trustedUuids);
            try {
                vaultsConfig.save(vaultsFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save trusted player", e);
            }
        }
    }

    /**
     * Removes a trusted player from a vault.
     */
    public void removeTrustedPlayer(Player owner, int vaultNumber, UUID trustedUuid) {
        String path = "vault-trust." + owner.getUniqueId().toString() + "." + vaultNumber;
        List<String> trustedUuids = new ArrayList<>(vaultsConfig.getStringList(path));
        trustedUuids.remove(trustedUuid.toString());
        vaultsConfig.set(path, trustedUuids);
        try {
            vaultsConfig.save(vaultsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove trusted player", e);
        }
    }

    /**
     * Checks if a player can access a vault (owner or trusted).
     */
    public boolean canAccessVault(Player player, UUID ownerUuid, int vaultNumber) {
        if (player.getUniqueId().equals(ownerUuid)) {
            return true; // Owner always has access
        }
        
        Player owner = Bukkit.getPlayer(ownerUuid);
        if (owner == null) {
            // Load from config if owner is offline
            String path = "vault-trust." + ownerUuid.toString() + "." + vaultNumber;
            List<String> trustedUuids = vaultsConfig.getStringList(path);
            return trustedUuids.contains(player.getUniqueId().toString());
        }
        
        List<UUID> trusted = getTrustedPlayers(owner, vaultNumber);
        return trusted.contains(player.getUniqueId());
    }

    /**
     * Shutdown and save all vaults.
     */
    public void shutdown() {
        saveVaults();
    }
}

