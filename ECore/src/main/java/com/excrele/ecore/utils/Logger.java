package com.excrele.ecore.utils;

import com.excrele.ecore.Ecore;

import java.util.logging.Level;

/**
 * Enhanced logging utility for Ecore plugin.
 * Provides structured logging with different log levels and detailed information.
 * 
 * @author Excrele
 * @version 1.0
 */
public class Logger {
    private final Ecore plugin;
    private final java.util.logging.Logger bukkitLogger;

    public Logger(Ecore plugin) {
        this.plugin = plugin;
        this.bukkitLogger = plugin.getLogger();
    }

    /**
     * Logs an info message with plugin prefix.
     * 
     * @param message The message to log
     */
    public void info(String message) {
        bukkitLogger.info("[Ecore] " + message);
    }

    /**
     * Logs a warning message with plugin prefix.
     * 
     * @param message The message to log
     */
    public void warning(String message) {
        bukkitLogger.warning("[Ecore] " + message);
    }

    /**
     * Logs a severe error message with plugin prefix.
     * 
     * @param message The message to log
     */
    public void severe(String message) {
        bukkitLogger.severe("[Ecore] " + message);
    }

    /**
     * Logs a debug message (only if debug mode is enabled).
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            bukkitLogger.info("[Ecore DEBUG] " + message);
        }
    }

    /**
     * Logs a detailed operation with context.
     * 
     * @param operation The operation being performed
     * @param context Additional context information
     * @param success Whether the operation succeeded
     */
    public void logOperation(String operation, String context, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("[%s] %s - %s", status, operation, context);
        
        if (success) {
            bukkitLogger.info("[Ecore] " + message);
        } else {
            bukkitLogger.warning("[Ecore] " + message);
        }
    }

    /**
     * Logs player-related actions with detailed information.
     * 
     * @param action The action performed
     * @param playerName The player involved
     * @param details Additional details
     */
    public void logPlayerAction(String action, String playerName, String details) {
        String message = String.format("Player Action: %s | Player: %s | Details: %s", action, playerName, details);
        bukkitLogger.info("[Ecore] " + message);
    }

    /**
     * Logs configuration-related messages.
     * 
     * @param message The configuration message
     */
    public void logConfig(String message) {
        bukkitLogger.info("[Ecore Config] " + message);
    }

    /**
     * Logs performance metrics.
     * 
     * @param metric The metric name
     * @param value The metric value
     * @param unit The unit of measurement
     */
    public void logPerformance(String metric, double value, String unit) {
        if (plugin.getConfig().getBoolean("debug.performance", false)) {
            String message = String.format("Performance: %s = %.2f %s", metric, value, unit);
            bukkitLogger.info("[Ecore Performance] " + message);
        }
    }

    /**
     * Logs an exception with full stack trace.
     * 
     * @param message The error message
     * @param exception The exception that occurred
     */
    public void logException(String message, Exception exception) {
        bukkitLogger.log(Level.SEVERE, "[Ecore] " + message, exception);
    }
}

