package com.telereceipt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telereceipt.controller.TelegramWebhookController;
import com.telereceipt.dto.telegram.TelegramUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@ConditionalOnProperty(name = "telegram.bot.polling.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramPollingService implements CommandLineRunner {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TelegramWebhookController webhookController;
    private final String botToken;
    private long lastUpdateId = 0;

    public TelegramPollingService(
            ObjectMapper objectMapper,
            TelegramWebhookController webhookController,
            @Value("${telegram.bot.token}") String botToken) {
        this.objectMapper = objectMapper;
        this.webhookController = webhookController;
        this.botToken = botToken;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void run(String... args) {
        // Start background polling thread
        Thread pollingThread = new Thread(this::pollLoop, "telegram-polling");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void pollLoop() {
        System.out.println("🤖 Telegram Bot Polling started for @DuitHilang_bot! Listening for messages...");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=10",
                        botToken, lastUpdateId + 1);

                String jsonResponse = restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(String.class);

                if (jsonResponse != null) {
                    JsonNode root = objectMapper.readTree(jsonResponse);
                    JsonNode resultNode = root.path("result");

                    if (resultNode.isArray()) {
                        List<TelegramUpdate> updates = objectMapper.convertValue(
                                resultNode,
                                new TypeReference<List<TelegramUpdate>>() {}
                        );

                        for (TelegramUpdate update : updates) {
                            if (update.getUpdateId() != null) {
                                lastUpdateId = Math.max(lastUpdateId, update.getUpdateId());
                            }
                            webhookController.handleTelegramUpdate(update);
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
