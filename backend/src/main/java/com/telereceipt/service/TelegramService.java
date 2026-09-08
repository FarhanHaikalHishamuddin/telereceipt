package com.telereceipt.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramService {

    private final RestClient restClient;
    private final String botToken;

    public TelegramService(@Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + botToken)
                .build();
    }

    @PostConstruct
    public void registerBotCommands() {
        try {
            List<Map<String, String>> commands = List.of(
                    Map.of("command", "start", "description", "Introduction & quick tour"),
                    Map.of("command", "budget", "description", "Set monthly budget (e.g. /budget 2500)"),
                    Map.of("command", "status", "description", "View current month spend vs budget"),
                    Map.of("command", "web", "description", "Get secure link to Web Dashboard"),
                    Map.of("command", "help", "description", "How to use DuitHilang bot")
            );
            Map<String, Object> payload = Map.of("commands", commands);
            restClient.post()
                    .uri("/setMyCommands")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("✅ Telegram bot commands registered successfully!");
        } catch (Exception e) {
            System.err.println("Notice: Could not register bot commands: " + e.getMessage());
        }
    }

    /**
     * Send a standard HTML-formatted message to a Telegram chat.
     */
    public void sendMessage(Long chatId, String htmlText) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", htmlText);
        payload.put("parse_mode", "HTML");

        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Failed to send Telegram message: " + e.getMessage());
        }
    }

    /**
     * Send a message with interactive inline buttons (e.g. Confirm / Edit).
     */
    public void sendMessageWithButtons(Long chatId, String htmlText, List<List<Map<String, String>>> keyboard) {
        Map<String, Object> replyMarkup = Map.of("inline_keyboard", keyboard);
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", htmlText);
        payload.put("parse_mode", "HTML");
        payload.put("reply_markup", replyMarkup);

        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Failed to send Telegram button message: " + e.getMessage());
        }
    }

    /**
     * Acknowledge button clicks from user.
     */
    public void answerCallbackQuery(String callbackQueryId, String notificationText) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("callback_query_id", callbackQueryId);
        if (notificationText != null) {
            payload.put("text", notificationText);
        }

        try {
            restClient.post()
                    .uri("/answerCallbackQuery")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Failed to answer callback query: " + e.getMessage());
        }
    }
}
