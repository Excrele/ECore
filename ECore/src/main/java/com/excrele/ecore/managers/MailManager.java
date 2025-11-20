package com.excrele.ecore.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.excrele.ecore.Ecore;

public class MailManager {
    private final Ecore plugin;
    private File mailFile;
    private FileConfiguration mailConfig;

    public MailManager(Ecore plugin) {
        this.plugin = plugin;
        initializeMailConfig();
    }

    private void initializeMailConfig() {
        mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) {
            try {
                mailFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create mail.yml", e);
            }
        }
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
    }

    public void sendMail(Player sender, Player recipient, String message) {
        String uuid = recipient.getUniqueId().toString();
        List<String> mailList = mailConfig.getStringList("mail." + uuid);
        
        String mailEntry = sender.getName() + ":" + message + ":" + System.currentTimeMillis();
        mailList.add(mailEntry);
        
        mailConfig.set("mail." + uuid, mailList);
        saveMail();
        
        sender.sendMessage("§aMail sent to " + recipient.getName() + "!");
        if (recipient.isOnline()) {
            recipient.sendMessage("§eYou have new mail! Use /mail read to view it.");
        }
    }

    public void sendMailToAll(Player sender, String message) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player != sender) {
                sendMail(sender, player, message);
            }
        }
        sender.sendMessage("§aMail sent to all online players!");
    }

    public List<MailEntry> getMail(Player player) {
        String uuid = player.getUniqueId().toString();
        List<String> mailList = mailConfig.getStringList("mail." + uuid);
        List<MailEntry> entries = new ArrayList<>();
        
        for (String mail : mailList) {
            String[] parts = mail.split(":", 3);
            if (parts.length == 3) {
                entries.add(new MailEntry(parts[0], parts[1], Long.parseLong(parts[2])));
            }
        }
        
        return entries;
    }

    public void clearMail(Player player) {
        String uuid = player.getUniqueId().toString();
        mailConfig.set("mail." + uuid, new ArrayList<>());
        saveMail();
    }

    public int getMailCount(Player player) {
        return getMail(player).size();
    }

    private void saveMail() {
        try {
            mailConfig.save(mailFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save mail.yml: " + e.getMessage());
        }
    }

    public static class MailEntry {
        private final String sender;
        private final String message;
        private final long timestamp;

        public MailEntry(String sender, String message, long timestamp) {
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}

