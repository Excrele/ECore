package com.excrele.ecore.integrations;

import com.excrele.ecore.Ecore;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault integration for economy compatibility.
 * Allows Ecore to work with Vault-compatible economy plugins.
 * Note: Vault dependency is optional. This class will gracefully handle its absence.
 * 
 * @author Excrele
 * @version 1.0
 */
public class VaultIntegration {
    private final Ecore plugin;
    private Object vaultEconomy; // Using Object to avoid compile-time dependency
    private boolean vaultEnabled = false;

    public VaultIntegration(Ecore plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            if (setupEconomy()) {
                vaultEnabled = true;
                plugin.getLogger().info("Vault integration enabled! Using Vault economy provider.");
            } else {
                plugin.getLogger().warning("Vault found but no economy provider registered. Using Ecore's built-in economy.");
            }
        } else {
            plugin.getLogger().info("Vault not found. Using Ecore's built-in economy.");
        }
    }

    private boolean setupEconomy() {
        try {
            // Use reflection to avoid compile-time dependency
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = plugin.getServer().getServicesManager()
                .getRegistration(economyClass);
            if (rsp == null) {
                return false;
            }
            vaultEconomy = rsp.getProvider();
            return vaultEconomy != null;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Vault economy class not found. Vault may not be properly installed.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error setting up Vault economy: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if Vault integration is enabled and available.
     * 
     * @return true if Vault is enabled and economy provider is available
     */
    public boolean isVaultEnabled() {
        return vaultEnabled && vaultEconomy != null;
    }

    /**
     * Gets the Vault economy provider.
     * 
     * @return The Economy instance, or null if not available
     */
    public Object getVaultEconomy() {
        return vaultEconomy;
    }

    /**
     * Checks if Ecore should use Vault for economy operations.
     * 
     * @return true if Vault should be used, false to use Ecore's built-in economy
     */
    public boolean shouldUseVault() {
        return isVaultEnabled() && plugin.getConfig().getBoolean("economy.use-vault", false);
    }
}

