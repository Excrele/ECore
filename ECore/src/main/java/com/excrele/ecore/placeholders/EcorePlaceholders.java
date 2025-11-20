package com.excrele.ecore.placeholders;

import com.excrele.ecore.Ecore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class EcorePlaceholders extends PlaceholderExpansion {
    private final Ecore plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public EcorePlaceholders(Ecore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "ecore";
    }

    @Override
    public String getAuthor() {
        return "Excrele";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) {
            return ""; // Return empty if player is offline
        }

        Player p = player.getPlayer();
        if (p == null) {
            return "";
        }

        // %ecore_homes% - Number of homes
        if (params.equalsIgnoreCase("homes")) {
            List<String> homes = plugin.getHomeManager().getHomes(p);
            return String.valueOf(homes.size());
        }

        // %ecore_balance% - Player balance
        if (params.equalsIgnoreCase("balance")) {
            double balance = plugin.getEconomyManager().getBalance(p.getUniqueId());
            return df.format(balance);
        }

        // %ecore_playtime% - Playtime formatted
        if (params.equalsIgnoreCase("playtime")) {
            // Calculate playtime from first join (if tracked)
            // For now, return a placeholder - you can enhance this with actual playtime tracking
            long playtime = plugin.getStatisticsManager().getStatistic(p, "playtime-seconds");
            if (playtime == 0) {
                return "0h 0m";
            }
            long hours = playtime / 3600;
            long minutes = (playtime % 3600) / 60;
            return hours + "h " + minutes + "m";
        }

        // %ecore_kills% - Number of kills
        if (params.equalsIgnoreCase("kills")) {
            return String.valueOf(plugin.getStatisticsManager().getStatistic(p, "kills"));
        }

        // %ecore_deaths% - Number of deaths
        if (params.equalsIgnoreCase("deaths")) {
            return String.valueOf(plugin.getStatisticsManager().getStatistic(p, "deaths"));
        }

        // %ecore_achievements% - Achievement count
        if (params.equalsIgnoreCase("achievements")) {
            if (plugin.getAchievementManager() != null) {
                return String.valueOf(plugin.getAchievementManager().getPlayerAchievements(p).size());
            }
            return "0";
        }

        // %ecore_max_homes% - Maximum homes allowed
        if (params.equalsIgnoreCase("max_homes")) {
            return String.valueOf(plugin.getConfigManager().getMaxHomes());
        }

        // %ecore_kdr% - Kill/Death ratio
        if (params.equalsIgnoreCase("kdr")) {
            int kills = plugin.getStatisticsManager().getStatistic(p, "kills");
            int deaths = plugin.getStatisticsManager().getStatistic(p, "deaths");
            if (deaths == 0) {
                return kills > 0 ? String.valueOf(kills) : "0.00";
            }
            double kdr = (double) kills / deaths;
            return df.format(kdr);
        }

        // %ecore_distance% - Distance traveled
        if (params.equalsIgnoreCase("distance")) {
            double distance = plugin.getStatisticsManager().getStatisticDouble(p, "distance-traveled");
            if (distance >= 1000) {
                return df.format(distance / 1000) + "km";
            }
            return df.format(distance) + "m";
        }

        // %ecore_items_crafted% - Items crafted
        if (params.equalsIgnoreCase("items_crafted")) {
            return String.valueOf(plugin.getStatisticsManager().getStatistic(p, "items-crafted"));
        }

        // %ecore_experience_gained% - Experience gained
        if (params.equalsIgnoreCase("experience_gained")) {
            return String.valueOf(plugin.getStatisticsManager().getStatistic(p, "experience-gained"));
        }

        // %ecore_damage_taken% - Damage taken
        if (params.equalsIgnoreCase("damage_taken")) {
            double damage = plugin.getStatisticsManager().getStatisticDouble(p, "damage-taken");
            return df.format(damage);
        }

        // %ecore_damage_dealt% - Damage dealt
        if (params.equalsIgnoreCase("damage_dealt")) {
            double damage = plugin.getStatisticsManager().getStatisticDouble(p, "damage-dealt");
            return df.format(damage);
        }

        // %ecore_joins% - Number of joins
        if (params.equalsIgnoreCase("joins")) {
            return String.valueOf(plugin.getStatisticsManager().getStatistic(p, "joins"));
        }

        // %ecore_mail_count% - Unread mail count
        if (params.equalsIgnoreCase("mail_count")) {
            return String.valueOf(plugin.getMailManager().getMailCount(p));
        }

        return null; // PlaceholderAPI will return null if placeholder is unknown
    }
}

