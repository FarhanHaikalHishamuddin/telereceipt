package com.telereceipt.controller;

import com.telereceipt.dto.telegram.TelegramUpdate;
import com.telereceipt.model.Expense;
import com.telereceipt.model.ExpenseCategory;
import com.telereceipt.model.ExpenseStatus;
import com.telereceipt.model.UserProfile;
import com.telereceipt.repository.ExpenseRepository;
import com.telereceipt.repository.UserProfileRepository;
import com.telereceipt.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final TelegramService telegramService;
    private final TextExpenseParser textExpenseParser;
    private final VisionReceiptService visionReceiptService;
    private final MinioStorageService minioStorageService;
    private final ExpenseRepository expenseRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuthTokenService authTokenService;

    public TelegramWebhookController(
            TelegramService telegramService,
            TextExpenseParser textExpenseParser,
            VisionReceiptService visionReceiptService,
            MinioStorageService minioStorageService,
            ExpenseRepository expenseRepository,
            UserProfileRepository userProfileRepository,
            AuthTokenService authTokenService) {
        this.telegramService = telegramService;
        this.textExpenseParser = textExpenseParser;
        this.visionReceiptService = visionReceiptService;
        this.minioStorageService = minioStorageService;
        this.expenseRepository = expenseRepository;
        this.userProfileRepository = userProfileRepository;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleTelegramUpdate(@RequestBody TelegramUpdate update) {
        if (update == null) {
            return ResponseEntity.ok().build();
        }

        if (update.getMessage() != null) {
            handleMessage(update.getMessage());
        }

        if (update.getCallbackQuery() != null) {
            handleCallbackQuery(update.getCallbackQuery());
        }

        return ResponseEntity.ok().build();
    }

    private void handleMessage(TelegramUpdate.TelegramMessage message) {
        Long chatId = message.getChat() != null ? message.getChat().getId() : null;
        Long userId = message.getFrom() != null ? message.getFrom().getId() : null;
        String text = message.getText();

        if (chatId == null || userId == null) {
            return;
        }

        // 1. Ensure user profile exists
        UserProfile profile = userProfileRepository.findById(userId).orElseGet(() -> {
            String username = message.getFrom().getUsername();
            String firstName = message.getFrom().getFirstName();
            UserProfile newProfile = new UserProfile(userId, username, firstName, new BigDecimal("2000.00"), "MYR", 0);
            return userProfileRepository.save(newProfile);
        });

        // 2. Handle /start command (Introduction tour)
        if (text != null && text.startsWith("/start")) {
            String name = message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "there";
            String welcome = String.format("""
                👋 <b>Welcome to DuitHilang Bot, %s!</b>

                I help you track your daily expenses with zero friction.

                ⚡ <b>Two Ways to Record:</b>
                • <b>Quick Text:</b> Type <code>25.50 lunch nasi kandar</code> or <code>15 grab</code>
                • <b>Receipt / Slip:</b> Snap any receipt photo or bank transfer screenshot!

                🎯 <b>Useful Commands:</b>
                • /budget [amount] — Set your monthly budget (e.g. <code>/budget 2500</code>)
                • /status — See how much you've spent this month
                • /web — Open your private visual Web Dashboard
                • /help — Show guide anytime

                💡 <i>Try typing</i> <code>10 mee goreng</code> <i>right now to record your first expense!</i>
                """, name);
            telegramService.sendMessage(chatId, welcome);
            return;
        }

        // 3. Handle /help command
        if (text != null && text.startsWith("/help")) {
            String help = """
                📖 <b>DuitHilang Bot Guide</b>

                <b>Text Formats:</b>
                • <code>18.50 kenangan coffee</code>
                • <code>45 petrol ron95</code>
                • <code>120 tnb bill</code>
                • <code>85 groceries lotus</code>

                <b>Photo / Slip Upload:</b>
                Upload any paper receipt or e-wallet screenshot (DuitNow, TNG, GrabPay, MAE). Our AI will read the store, amount, date, and category automatically!

                <b>Commands:</b>
                /status — View current spending breakdown
                /budget 3000 — Change monthly budget target
                /web — Access your personal web charts & CSV export
                """;
            telegramService.sendMessage(chatId, help);
            return;
        }

        // 4. Handle /budget command
        if (text != null && text.startsWith("/budget")) {
            String param = text.replace("/budget", "").trim();
            if (!param.isEmpty()) {
                try {
                    String cleanNum = param.replaceAll("(?i)rm", "").trim();
                    BigDecimal newBudget = new BigDecimal(cleanNum).setScale(2, RoundingMode.HALF_UP);
                    profile.setMonthlyBudget(newBudget);
                    profile.setLastWarningThreshold(0); // Reset warning tracker
                    userProfileRepository.save(profile);

                    String msg = String.format(
                        "🎯 <b>Monthly Budget Updated!</b>\n\nYour target budget is now <b>RM %.2f</b>.\nI will proactively alert you if your spending reaches 80%% or 100%% of this limit.",
                        newBudget
                    );
                    telegramService.sendMessage(chatId, msg);
                    return;
                } catch (Exception ignored) {}
            }

            String currentBudgetMsg = String.format(
                "🎯 <b>Your Current Monthly Budget:</b> RM %.2f\n\nTo change it, type: <code>/budget 2500</code> (or any amount).",
                profile.getMonthlyBudget()
            );
            telegramService.sendMessage(chatId, currentBudgetMsg);
            return;
        }

        // 5. Handle /status command
        if (text != null && text.startsWith("/status")) {
            LocalDate now = LocalDate.now();
            LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());

            BigDecimal spent = expenseRepository.sumAmountByUserIdAndDateRange(userId, start, end, ExpenseStatus.CONFIRMED);
            if (spent == null) spent = BigDecimal.ZERO;

            BigDecimal budget = profile.getMonthlyBudget();
            BigDecimal remaining = budget.subtract(spent);
            int pct = budget.compareTo(BigDecimal.ZERO) > 0 
                ? spent.multiply(BigDecimal.valueOf(100)).divide(budget, 1, RoundingMode.HALF_UP).intValue() 
                : 0;

            String alertNote = (pct >= 100) 
                ? "🚨 <b>Warning: You have exceeded your monthly budget!</b>"
                : (pct >= 80)
                ? "⚠️ <b>Warning: You have reached 80% of your budget!</b>"
                : "✅ <i>Your spending is currently within healthy limits.</i>";

            String statusMsg = String.format("""
                📊 <b>Monthly Spending Status (%s)</b>

                💰 <b>Spent So Far:</b> RM %.2f
                🎯 <b>Monthly Budget:</b> RM %.2f
                📈 <b>Remaining:</b> RM %.2f (%d%% used)

                %s
                """,
                now.getMonth().name(),
                spent,
                budget,
                remaining.max(BigDecimal.ZERO),
                pct,
                alertNote
            );

            String token = authTokenService.generateToken(userId);
            String dashboardUrl = frontendUrl + "/?token=" + token;
            String fullStatusMsg = statusMsg + String.format("\n👉 <a href=\"%s\"><b>Open Web Dashboard</b></a>", dashboardUrl);
            telegramService.sendMessage(chatId, fullStatusMsg);
            return;
        }

        // 6. Handle /web command (Magic link login)
        if (text != null && text.startsWith("/web")) {
            String token = authTokenService.generateToken(userId);
            String dashboardUrl = frontendUrl + "/?token=" + token;
            String webMsg = String.format("""
                🔐 <b>Your Private Web Dashboard</b>

                Click below to view your interactive charts and transactions:
                👉 <a href="%s"><b>Open My Dashboard</b></a>

                🔗 Or copy this link into your browser:
                <code>%s</code>

                ⏱ <i>This link is secure and valid for 30 minutes.</i>
                """, dashboardUrl, dashboardUrl);
            telegramService.sendMessage(chatId, webMsg);
            return;
        }

        // 7. Handle Quick Text Logging
        if (text != null && !text.trim().isEmpty()) {
            TextExpenseParser.ParsedTextExpense parsed = textExpenseParser.parse(text);

            if (parsed.valid()) {
                ExpenseCategory category = parsed.category();
                if (category == ExpenseCategory.OTHER && parsed.merchant() != null && !parsed.merchant().equalsIgnoreCase("Expense")) {
                    ExpenseCategory aiCategory = visionReceiptService.classifyCategory(parsed.merchant());
                    if (aiCategory != null && aiCategory != ExpenseCategory.OTHER) {
                        category = aiCategory;
                    }
                }

                Expense expense = Expense.builder()
                        .telegramUserId(userId)
                        .merchant(parsed.merchant())
                        .amount(parsed.amount())
                        .category(category)
                        .status(ExpenseStatus.CONFIRMED)
                        .transactionDate(LocalDate.now())
                        .notes(parsed.notes())
                        .build();

                expenseRepository.save(expense);

                String reply = String.format(
                        "✅ <b>Expense Recorded!</b>\n\n💰 <b>Amount:</b> RM %.2f\n📂 <b>Category:</b> %s\n📝 <b>Note:</b> %s",
                        parsed.amount(),
                        category,
                        parsed.notes()
                );
                telegramService.sendMessage(chatId, reply);

                // Proactive budget alert check
                checkAndSendBudgetAlert(userId, chatId, profile);
            } else {
                String helpReply = """
                    ⚠️ Couldn't find an amount in your message.
                    Try typing:
                    • <code>25.50 lunch nasi kandar</code>
                    • <code>12 coffee</code>
                    • <code>50 petrol</code>
                    """;
                telegramService.sendMessage(chatId, helpReply);
            }
            return;
        }

        // 8. Handle Photo Upload with Vision AI
        if (message.getPhoto() != null && !message.getPhoto().isEmpty()) {
            var photos = message.getPhoto();
            var largestPhoto = photos.get(photos.size() - 1);

            telegramService.sendMessage(chatId, "📸 <i>Analyzing your receipt with AI...</i>");

            byte[] imageBytes = visionReceiptService.downloadTelegramFile(largestPhoto.getFileId());
            if (imageBytes == null) {
                telegramService.sendMessage(chatId, "❌ Failed to download receipt image. Please try again.");
                return;
            }

            // Upload image to MinIO / S3
            String objectName = "receipts/" + userId + "/" + UUID.randomUUID() + ".jpg";
            minioStorageService.uploadImage(objectName, imageBytes, "image/jpeg");

            // Extract receipt data via Gemini Vision AI
            VisionReceiptService.VisionExtractionResult result =
                    visionReceiptService.extractReceiptData(imageBytes, "image/jpeg");

            // Persist as PENDING until confirmed
            Expense pendingExpense = Expense.builder()
                    .telegramUserId(userId)
                    .merchant(result.merchant())
                    .amount(result.totalAmount())
                    .category(result.category())
                    .status(ExpenseStatus.PENDING)
                    .transactionDate(result.transactionDate())
                    .receiptImagePath(objectName)
                    .notes(result.merchant())
                    .build();

            pendingExpense = expenseRepository.save(pendingExpense);

            String card = String.format("""
                🧾 <b>Receipt Detected!</b>

                🏪 <b>Merchant:</b> %s
                💰 <b>Amount:</b> RM %.2f
                📂 <b>Category:</b> %s
                📅 <b>Date:</b> %s

                Please confirm to record this expense:
                """,
                result.merchant(),
                result.totalAmount(),
                result.category(),
                result.transactionDate()
            );

            List<List<Map<String, String>>> keyboard = List.of(
                    List.of(
                            Map.of("text", "✅ Confirm", "callback_data", "CONFIRM_" + pendingExpense.getId()),
                            Map.of("text", "❌ Cancel", "callback_data", "CANCEL_" + pendingExpense.getId())
                    )
            );

            telegramService.sendMessageWithButtons(chatId, card, keyboard);
        }
    }

    private void handleCallbackQuery(TelegramUpdate.TelegramCallbackQuery query) {
        String data = query.getData();
        Long chatId = query.getMessage() != null && query.getMessage().getChat() != null
                ? query.getMessage().getChat().getId() : null;

        if (data != null && data.startsWith("CONFIRM_")) {
            try {
                Long id = Long.parseLong(data.substring("CONFIRM_".length()));
                expenseRepository.findById(id).ifPresent(e -> {
                    e.setStatus(ExpenseStatus.CONFIRMED);
                    expenseRepository.save(e);

                    // Check budget alerts
                    userProfileRepository.findById(e.getTelegramUserId()).ifPresent(p -> {
                        checkAndSendBudgetAlert(e.getTelegramUserId(), chatId, p);
                    });
                });
                telegramService.answerCallbackQuery(query.getId(), "Expense confirmed!");
                if (chatId != null) {
                    telegramService.sendMessage(chatId, "✅ <b>Expense confirmed and saved to your dashboard!</b>");
                }
            } catch (Exception ignored) {}
        } else if (data != null && data.startsWith("CANCEL_")) {
            try {
                Long id = Long.parseLong(data.substring("CANCEL_".length()));
                expenseRepository.findById(id).ifPresent(e -> {
                    e.setStatus(ExpenseStatus.CANCELLED);
                    expenseRepository.save(e);
                });
                telegramService.answerCallbackQuery(query.getId(), "Expense cancelled.");
                if (chatId != null) {
                    telegramService.sendMessage(chatId, "❌ <i>Expense discarded.</i>");
                }
            } catch (Exception ignored) {}
        }
    }

    private void checkAndSendBudgetAlert(Long userId, Long chatId, UserProfile profile) {
        if (profile == null || chatId == null) return;
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = now.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal spent = expenseRepository.sumAmountByUserIdAndDateRange(userId, start, end, ExpenseStatus.CONFIRMED);
        if (spent == null) spent = BigDecimal.ZERO;
        BigDecimal budget = profile.getMonthlyBudget();

        if (budget.compareTo(BigDecimal.ZERO) <= 0) return;

        int pct = spent.multiply(BigDecimal.valueOf(100)).divide(budget, 0, RoundingMode.HALF_UP).intValue();

        if (pct >= 100 && profile.getLastWarningThreshold() < 100) {
            profile.setLastWarningThreshold(100);
            userProfileRepository.save(profile);
            String alert = String.format(
                "🚨 <b>Budget Exceeded!</b>\n\nYou have spent <b>RM %.2f</b> this month, exceeding your monthly budget of <b>RM %.2f</b> (%d%% used)!",
                spent, budget, pct
            );
            telegramService.sendMessage(chatId, alert);
        } else if (pct >= 80 && profile.getLastWarningThreshold() < 80) {
            profile.setLastWarningThreshold(80);
            userProfileRepository.save(profile);
            String alert = String.format(
                "⚠️ <b>Budget Warning (80%% Reached)!</b>\n\nYou have spent <b>RM %.2f</b> of your <b>RM %.2f</b> monthly budget (%d%% used). Watch your expenses!",
                spent, budget, pct
            );
            telegramService.sendMessage(chatId, alert);
        }
    }
}
