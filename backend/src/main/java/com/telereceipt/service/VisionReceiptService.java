package com.telereceipt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telereceipt.model.ExpenseCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class VisionReceiptService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String botToken;
    private final String geminiApiKey;
    private final String geminiModel;

    public record VisionExtractionResult(
            boolean success,
            String merchant,
            BigDecimal totalAmount,
            LocalDate transactionDate,
            ExpenseCategory category,
            String rawJson
    ) {}

    public VisionReceiptService(
            ObjectMapper objectMapper,
            @Value("${telegram.bot.token}") String botToken,
            @Value("${ai.gemini.api-key:}") String geminiApiKey,
            @Value("${ai.gemini.model:gemini-3.6-flash}") String geminiModel) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
        this.botToken = botToken;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
    }

    /**
     * Download the raw image file from Telegram servers using file_id.
     */
    public byte[] downloadTelegramFile(String fileId) {
        try {
            // 1. Get file path from Telegram
            String getFileUrl = String.format("https://api.telegram.org/bot%s/getFile?file_id=%s", botToken, fileId);
            String responseBody = restClient.get()
                    .uri(getFileUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String filePath = root.path("result").path("file_path").asText();
            if (filePath == null || filePath.isEmpty()) {
                return null;
            }

            // 2. Download binary bytes
            String downloadUrl = String.format("https://api.telegram.org/file/bot%s/%s", botToken, filePath);
            return restClient.get()
                    .uri(downloadUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            System.err.println("Failed to download Telegram file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extract structured receipt details using Gemini Flash Vision API.
     */
    public VisionExtractionResult extractReceiptData(byte[] imageBytes, String mimeType) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            // Mock fallback if API key is not yet set
            return new VisionExtractionResult(
                    true,
                    "Receipt Merchant",
                    new BigDecimal("18.50"),
                    LocalDate.now(),
                    ExpenseCategory.FOOD,
                    "{}"
            );
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String prompt = """
                Analyze this receipt, invoice, or digital payment transfer screenshot.
                Extract the details in strict JSON with the following keys:
                - "merchant": the well-known consumer brand or store name (e.g. "McDonald's", "Starbucks", "Shell") rather than the legal company entity if visible
                - "total_amount": the final net amount paid after taxes, discounts, and roundings as a numeric float (e.g. 33.90)
                - "date": transaction date in YYYY-MM-DD format (use today's date if missing or unreadable)
                - "category": choose strictly one of: FOOD, TRANSPORT, GROCERIES, UTILITIES, SHOPPING, ENTERTAINMENT, OTHER
                """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType != null ? mimeType : "image/jpeg",
                                            "data", base64Image
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json"
                    )
            );

            String geminiUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", geminiModel, geminiApiKey);

            String response = restClient.post()
                    .uri(geminiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String jsonText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            JsonNode parsedReceipt = objectMapper.readTree(jsonText);

            String merchant = parsedReceipt.path("merchant").asText("Receipt Merchant");
            double amountDouble = parsedReceipt.path("total_amount").asDouble(0.0);
            BigDecimal amount = BigDecimal.valueOf(amountDouble).setScale(2, RoundingMode.HALF_UP);
            
            String dateStr = parsedReceipt.path("date").asText();
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ex) {
                date = LocalDate.now();
            }

            String catStr = parsedReceipt.path("category").asText("OTHER").toUpperCase();
            ExpenseCategory category;
            try {
                category = ExpenseCategory.valueOf(catStr);
            } catch (Exception ex) {
                category = ExpenseCategory.OTHER;
            }

            return new VisionExtractionResult(true, merchant, amount, date, category, jsonText);

        } catch (Exception e) {
            System.err.println("Vision AI extraction failed: " + e.getMessage());
            e.printStackTrace();
            return new VisionExtractionResult(
                    false,
                    "Unknown Merchant",
                    BigDecimal.ZERO,
                    LocalDate.now(),
                    ExpenseCategory.OTHER,
                    "{}"
            );
        }
    }

    /**
     * Semantically classify an expense description into a standard category using Gemini.
     */
    public ExpenseCategory classifyCategory(String description) {
        if (description == null || description.trim().isEmpty() || geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            return ExpenseCategory.OTHER;
        }

        try {
            String prompt = String.format("""
                Classify this expense description into strictly ONE of these categories:
                FOOD, TRANSPORT, GROCERIES, UTILITIES, SHOPPING, ENTERTAINMENT, OTHER.
                
                Description: "%s"
                
                Respond in strict JSON with the key "category":
                {"category": "FOOD"}
                """, description.trim());

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json"
                    )
            );

            String geminiUrl = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", geminiModel, geminiApiKey);

            String response = restClient.post()
                    .uri(geminiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            String jsonText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            JsonNode parsed = objectMapper.readTree(jsonText);
            String catStr = parsed.path("category").asText("OTHER").trim().toUpperCase();

            return ExpenseCategory.valueOf(catStr);
        } catch (Exception e) {
            System.err.println("Gemini category classification failed for '" + description + "': " + e.getMessage());
            return ExpenseCategory.OTHER;
        }
    }
}
