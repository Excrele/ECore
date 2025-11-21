package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ReportManager {
    private final Ecore plugin;
    private File reportFile;
    private FileConfiguration reportConfig;

    public ReportManager(Ecore plugin) {
        this.plugin = plugin;
        initializeReportConfig();
    }

    private void initializeReportConfig() {
        reportFile = new File(plugin.getDataFolder(), "reports.yml");
        if (!reportFile.exists()) {
            try {
                reportFile.getParentFile().mkdirs();
                reportFile.createNewFile();
                reportConfig = YamlConfiguration.loadConfiguration(reportFile);
                reportConfig.set("reports", new java.util.LinkedHashMap<String, Object>());
                reportConfig.save(reportFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create reports.yml: " + e.getMessage());
                reportConfig = new YamlConfiguration();
            }
        } else {
            reportConfig = YamlConfiguration.loadConfiguration(reportFile);
        }
    }

    public void submitReport(String reporter, String target, String reason) {
        String reportId = String.valueOf(System.currentTimeMillis());
        reportConfig.set("reports." + reportId + ".reporter", reporter);
        reportConfig.set("reports." + reportId + ".target", target);
        reportConfig.set("reports." + reportId + ".reason", reason);
        reportConfig.set("reports." + reportId + ".timestamp", System.currentTimeMillis());

        try {
            reportConfig.save(reportFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save report to reports.yml: " + e.getMessage());
        }

        plugin.getDiscordManager().sendStaffLogNotification(
                "report-log",
                reporter,
                "submitted report",
                target,
                reason
        );
    }

    public void createReport(String reporter, String target, String reason) {
        submitReport(reporter, target, reason);
    }

    public java.util.Set<String> getReportIds() {
        if (!reportConfig.contains("reports")) {
            return new java.util.HashSet<>();
        }
        return reportConfig.getConfigurationSection("reports").getKeys(false);
    }

    public String getReporter(String reportId) {
        return reportConfig.getString("reports." + reportId + ".reporter");
    }

    public String getTarget(String reportId) {
        return reportConfig.getString("reports." + reportId + ".target");
    }

    public String getReason(String reportId) {
        return reportConfig.getString("reports." + reportId + ".reason");
    }

    public long getTimestamp(String reportId) {
        return reportConfig.getLong("reports." + reportId + ".timestamp", 0);
    }

    public boolean isResolved(String reportId) {
        return reportConfig.getBoolean("reports." + reportId + ".resolved", false);
    }

    public void resolveReport(String reportId, String resolver, String notes) {
        reportConfig.set("reports." + reportId + ".resolved", true);
        reportConfig.set("reports." + reportId + ".resolver", resolver);
        reportConfig.set("reports." + reportId + ".resolution-notes", notes);
        reportConfig.set("reports." + reportId + ".resolved-time", System.currentTimeMillis());
        try {
            reportConfig.save(reportFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save report resolution: " + e.getMessage());
        }
    }
}