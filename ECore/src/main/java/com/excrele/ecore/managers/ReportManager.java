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
            plugin.saveResource("reports.yml", false);
        }
        reportConfig = YamlConfiguration.loadConfiguration(reportFile);
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

    // Alias for submitReport to resolve compilation error
    public void createReport(String reporter, String target, String reason) {
        submitReport(reporter, target, reason);
    }
}