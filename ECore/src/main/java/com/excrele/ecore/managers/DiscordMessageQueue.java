package com.excrele.ecore.managers;

import com.excrele.ecore.Ecore;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Message queue system for Discord messages with retry logic.
 * Queues messages during Discord outages and sends them when reconnected.
 */
public class DiscordMessageQueue {
    private final Ecore plugin;
    private final ConcurrentLinkedQueue<QueuedMessage> messageQueue;
    private final int maxRetries = 3;
    private final long retryDelay = 5000; // 5 seconds

    public DiscordMessageQueue(Ecore plugin) {
        this.plugin = plugin;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Queues a plain text message to be sent.
     */
    public void queueMessage(String channelId, String message) {
        messageQueue.offer(new QueuedMessage(channelId, message, null, 0));
    }

    /**
     * Queues an embed message to be sent.
     */
    public void queueEmbed(String channelId, MessageEmbed embed) {
        messageQueue.offer(new QueuedMessage(channelId, null, embed, 0));
    }

    /**
     * Processes the message queue and sends all queued messages.
     */
    public void processQueue(TextChannel channel) {
        if (channel == null) return;
        
        while (!messageQueue.isEmpty()) {
            QueuedMessage queued = messageQueue.poll();
            if (queued == null) break;

            if (!queued.getChannelId().equals(channel.getId())) {
                // Wrong channel, put it back
                messageQueue.offer(queued);
                continue;
            }

            try {
                if (queued.getEmbed() != null) {
                    channel.sendMessageEmbeds(queued.getEmbed()).queue(
                        null,
                        error -> handleSendError(queued, error)
                    );
                } else if (queued.getMessage() != null) {
                    channel.sendMessage(queued.getMessage()).queue(
                        null,
                        error -> handleSendError(queued, error)
                    );
                }
            } catch (Exception e) {
                handleSendError(queued, e);
            }
        }
    }

    private void handleSendError(QueuedMessage queued, Throwable error) {
        queued.incrementRetries();
        if (queued.getRetries() < maxRetries) {
            // Retry after delay
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                messageQueue.offer(queued);
            }, retryDelay / 50); // Convert to ticks
        } else {
            plugin.getLogger().log(Level.WARNING, 
                "Failed to send queued Discord message after " + maxRetries + " retries: " + error.getMessage());
        }
    }

    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        return messageQueue.size();
    }

    /**
     * Clears the message queue.
     */
    public void clearQueue() {
        messageQueue.clear();
    }

    private static class QueuedMessage {
        private final String channelId;
        private final String message;
        private final MessageEmbed embed;
        private int retries;

        public QueuedMessage(String channelId, String message, MessageEmbed embed, int retries) {
            this.channelId = channelId;
            this.message = message;
            this.embed = embed;
            this.retries = retries;
        }

        public String getChannelId() { return channelId; }
        public String getMessage() { return message; }
        public MessageEmbed getEmbed() { return embed; }
        public int getRetries() { return retries; }
        public void incrementRetries() { retries++; }
    }
}

