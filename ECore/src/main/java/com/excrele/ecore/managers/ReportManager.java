package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {
    private final Ecore plugin;
    private final File reportFile;
    private FileConfiguration reportConfig;
    private final List<Report> reports;
    private int nextReportId;

    public ReportManager(Ecore plugin) {
        this.plugin = plugin;
        this.reportFile = new File(plugin.getDataFolder(), "reports.yml");
        this.reportConfig = YamlConfiguration.loadConfiguration(reportFile);
        this.reports = new ArrayList<>();
        this.nextReportId = 1;
    }

    // Report class to store report data
    public static class Report {
        private final int id;
        private final String reporter;
        private final String target;
        private final String reason;
        private final String timestamp;

        public Report(int id, String reporter, String target, String reason, String timestamp) {
            this.id = id;
            this.reporter = reporter;
            this.target = target;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public String getReporter() {
            return reporter;
        }

        public String getTarget() {
            return target;
        }

        public String getReason() {
            return reason;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    // Create a new report
    public boolean createReport(String reporter, String target, String reason) {
        int maxReports = plugin.getConfigManager().getConfig().getInt("report.max-reports", 5);
        long cooldown = plugin.getConfigManager().getConfig().getLong("report.report-cooldown", 300);
        long currentTime = System.currentTimeMillis() / 1000;

        // Check report limits and cooldown
        int playerReports = 0;
        for (Report report : reports) {
            if (report.getReporter().equals(reporter)) {
                playerReports++;
                long reportTime = LocalDateTime.parse(report.getTimestamp()).toEpochSecond(java.time.ZoneOffset.UTC);
                if (currentTime - reportTime < cooldown) {
                    return false; // Cooldown not expired
                }
            }
        }
        if (playerReports >= maxReports) {
            return false; // Max reports reached
        }

        // Create and add report
        Report report = new Report(nextReportId++, reporter, target, reason, LocalDateTime.now().toString());
        reports.add(report);
        saveReports();
        return true;
    }

    // Get all reports
    public List<Report> getReports() {
        return new ArrayList<>(reports);
    }

    // Resolve (remove) a report by ID
    public void resolveReport(int id) {
        reports.removeIf(report -> report.getId() == id);
        saveReports();
    }

    // Load reports from file
    public void loadReports() {
        if (!reportFile.exists()) return;

        reportConfig = YamlConfiguration.loadConfiguration(reportFile);
        for (String id : reportConfig.getKeys(false)) {
            String reporter = reportConfig.getString(id + ".reporter");
            String target = reportConfig.getString(id + ".target");
            String reason = reportConfig.getString(id + ".reason");
            String timestamp = reportConfig.getString(id + ".timestamp");
            reports.add(new Report(Integer.parseInt(id), reporter, target, reason, timestamp));
            nextReportId = Math.max(nextReportId, Integer.parseInt(id) + 1);
        }
    }

    // Save reports to file
    public void saveReports() {
        reportConfig = new YamlConfiguration();
        for (Report report : reports) {
            String id = String.valueOf(report.getId());
            reportConfig.set(id + ".reporter", report.getReporter());
            reportConfig.set(id + ".target", report.getTarget());
            reportConfig.set(id + ".reason", report.getReason());
            reportConfig.set(id + ".timestamp", report.getTimestamp());
        }
        try {
            reportConfig.save(reportFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save reports: " + e.getMessage());
        }
    }
}