package com.telereceipt.controller;

import com.telereceipt.dto.AnalyticsSummaryResponse;
import com.telereceipt.dto.CategorySummaryResponse;
import com.telereceipt.model.Expense;
import com.telereceipt.model.ExpenseCategory;
import com.telereceipt.model.ExpenseStatus;
import com.telereceipt.model.MonthlyBudget;
import com.telereceipt.model.UserProfile;
import com.telereceipt.repository.ExpenseRepository;
import com.telereceipt.repository.MonthlyBudgetRepository;
import com.telereceipt.repository.UserProfileRepository;
import com.telereceipt.service.AuthTokenService;
import com.telereceipt.service.MinioStorageService;
import com.telereceipt.service.TelegramService;
import com.telereceipt.service.VisionReceiptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final MinioStorageService minioStorageService;
    private final AuthTokenService authTokenService;
    private final UserProfileRepository userProfileRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final VisionReceiptService visionReceiptService;
    private final TelegramService telegramService;

    public ExpenseController(
            ExpenseRepository expenseRepository,
            MinioStorageService minioStorageService,
            AuthTokenService authTokenService,
            UserProfileRepository userProfileRepository,
            MonthlyBudgetRepository monthlyBudgetRepository,
            VisionReceiptService visionReceiptService,
            TelegramService telegramService) {
        this.expenseRepository = expenseRepository;
        this.minioStorageService = minioStorageService;
        this.authTokenService = authTokenService;
        this.userProfileRepository = userProfileRepository;
        this.monthlyBudgetRepository = monthlyBudgetRepository;
        this.visionReceiptService = visionReceiptService;
        this.telegramService = telegramService;
    }

    public record UpdateExpenseRequest(
            String merchant,
            BigDecimal amount,
            ExpenseCategory category,
            LocalDate transactionDate,
            String notes
    ) {}

    public record BudgetUpdateRequest(
            BigDecimal budget,
            Integer year,
            Integer month
    ) {}

    public record CreateExpenseRequest(
            String merchant,
            BigDecimal amount,
            ExpenseCategory category,
            LocalDate transactionDate,
            String notes,
            String receiptImagePath
    ) {}

    /**
     * Helper to resolve userId from Bearer token, query param, or fallback to first active user.
     */
    private Long resolveUserId(String authHeader, String tokenParam) {
        String token = tokenParam;
        if ((token == null || token.isEmpty()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }
        if (token != null && !token.isEmpty()) {
            Long userId = authTokenService.validateToken(token);
            if (userId != null) {
                return userId;
            }
        }
        // Fallback for single-user testing when token is omitted
        var allProfiles = userProfileRepository.findAll();
        if (!allProfiles.isEmpty()) {
            return allProfiles.get(allProfiles.size() - 1).getTelegramUserId();
        }
        return null;
    }

    /**
     * Get paginated expenses for current authenticated user.
     */
    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
        Page<Expense> expensePage;
        if (year != null && month != null) {
            LocalDate baseDate = LocalDate.of(year, month, 1);
            LocalDate start = baseDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = baseDate.with(TemporalAdjusters.lastDayOfMonth());
            expensePage = expenseRepository.findByTelegramUserIdAndStatusAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                    userId, ExpenseStatus.CONFIRMED, start, end, pageRequest);
        } else {
            expensePage = expenseRepository.findByTelegramUserIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
                    userId, ExpenseStatus.CONFIRMED, pageRequest);
        }
        return ResponseEntity.ok(expensePage.getContent());
    }

    /**
     * Upload and analyze receipt photo using Gemini Vision AI.
     */
    @PostMapping(value = "/expenses/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No image file provided."));
        }

        try {
            byte[] imageBytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String extension = contentType.contains("png") ? ".png" : ".jpg";
            String objectName = "receipts/" + userId + "/" + UUID.randomUUID() + extension;

            // Upload image to MinIO
            minioStorageService.uploadImage(objectName, imageBytes, contentType);

            // Extract receipt data via Gemini Vision AI
            VisionReceiptService.VisionExtractionResult result =
                    visionReceiptService.extractReceiptData(imageBytes, contentType);

            return ResponseEntity.ok(Map.of(
                    "success", result.success(),
                    "merchant", result.merchant() != null ? result.merchant() : "",
                    "totalAmount", result.totalAmount() != null ? result.totalAmount() : BigDecimal.ZERO,
                    "transactionDate", result.transactionDate() != null ? result.transactionDate().toString() : LocalDate.now().toString(),
                    "category", result.category() != null ? result.category().name() : "OTHER",
                    "receiptImagePath", objectName
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process receipt: " + e.getMessage()));
        }
    }

    /**
     * Create a new confirmed expense directly from the web.
     */
    @PostMapping("/expenses")
    public ResponseEntity<?> createExpense(
            @RequestBody CreateExpenseRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than zero."));
        }

        Expense expense = Expense.builder()
                .telegramUserId(userId)
                .merchant(request.merchant() != null ? request.merchant().trim() : "Unknown")
                .amount(request.amount())
                .category(request.category() != null ? request.category() : ExpenseCategory.OTHER)
                .transactionDate(request.transactionDate() != null ? request.transactionDate() : LocalDate.now())
                .notes(request.notes() != null ? request.notes().trim() : "")
                .receiptImagePath(request.receiptImagePath())
                .status(ExpenseStatus.CONFIRMED)
                .build();

        Expense saved = expenseRepository.save(expense);

        // Check proactive budget alert and notify Telegram if threshold reached
        userProfileRepository.findById(userId).ifPresent(profile -> checkAndSendBudgetAlert(userId, profile));

        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing expense (merchant, amount, category, notes, date).
     */
    @PutMapping("/expenses/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @RequestBody UpdateExpenseRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);

        return expenseRepository.findById(id)
                .map(expense -> {
                    if (userId != null && !expense.getTelegramUserId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Expense>build();
                    }

                    if (request.merchant() != null) expense.setMerchant(request.merchant().trim());
                    if (request.amount() != null) expense.setAmount(request.amount());
                    if (request.category() != null) expense.setCategory(request.category());
                    if (request.transactionDate() != null) expense.setTransactionDate(request.transactionDate());
                    if (request.notes() != null) expense.setNotes(request.notes().trim());
                    expense.setStatus(ExpenseStatus.CONFIRMED);

                    Expense saved = expenseRepository.save(expense);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an expense by ID.
     */
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);

        return expenseRepository.findById(id)
                .map(expense -> {
                    if (userId != null && !expense.getTelegramUserId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    expenseRepository.delete(expense);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generate secure presigned URL to view receipt image.
     */
    @GetMapping("/expenses/{id}/receipt-url")
    public ResponseEntity<Map<String, String>> getReceiptUrl(@PathVariable Long id) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    String presignedUrl = minioStorageService.getPresignedUrl(expense.getReceiptImagePath());
                    return ResponseEntity.ok(Map.of("url", presignedUrl != null ? presignedUrl : ""));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Monthly budget and spend analytics summary.
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.ok(new AnalyticsSummaryResponse(BigDecimal.ZERO, new BigDecimal("2000.00"), 0, 0, "FOOD"));
        }

        LocalDate baseDate = (year != null && month != null)
                ? LocalDate.of(year, month, 1)
                : LocalDate.now();
        LocalDate startOfMonth = baseDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = baseDate.with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal monthlyBudget = null;
        if (year != null && month != null) {
            monthlyBudget = monthlyBudgetRepository.findByTelegramUserIdAndYearAndMonth(userId, year, month)
                    .map(MonthlyBudget::getBudget)
                    .orElse(null);
        }
        if (monthlyBudget == null) {
            monthlyBudget = userProfileRepository.findById(userId)
                    .map(UserProfile::getMonthlyBudget)
                    .orElse(new BigDecimal("2000.00"));
        }

        BigDecimal totalSpent = expenseRepository.sumAmountByUserIdAndDateRange(
                userId, startOfMonth, endOfMonth, ExpenseStatus.CONFIRMED);
        if (totalSpent == null) totalSpent = BigDecimal.ZERO;

        List<Expense> monthlyExpenses = expenseRepository.findByTelegramUserIdAndTransactionDateBetweenAndStatus(
                userId, startOfMonth, endOfMonth, ExpenseStatus.CONFIRMED);

        long count = monthlyExpenses.size();

        int percentage = monthlyBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(BigDecimal.valueOf(100)).divide(monthlyBudget, 0, RoundingMode.HALF_UP).intValue()
                : 0;

        AnalyticsSummaryResponse summary = AnalyticsSummaryResponse.builder()
                .totalSpentMonth(totalSpent)
                .monthlyBudget(monthlyBudget)
                .budgetPercentageUsed(Math.min(percentage, 100))
                .transactionCount(count)
                .topCategory("FOOD")
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Category spending breakdown for donut chart.
     */
    @GetMapping("/analytics/categories")
    public ResponseEntity<List<CategorySummaryResponse>> getCategoryBreakdown(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }

        LocalDate baseDate = (year != null && month != null)
                ? LocalDate.of(year, month, 1)
                : LocalDate.now();
        LocalDate startOfMonth = baseDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = baseDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Expense> monthlyExpenses = expenseRepository.findByTelegramUserIdAndTransactionDateBetweenAndStatus(
                userId, startOfMonth, endOfMonth, ExpenseStatus.CONFIRMED);

        Map<ExpenseCategory, BigDecimal> grouped = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        List<CategorySummaryResponse> results = grouped.entrySet().stream()
                .map(entry -> new CategorySummaryResponse(entry.getKey(), entry.getValue()))
                .toList();

        return ResponseEntity.ok(results);
    }

    public record DailySpendResponse(String date, int day, BigDecimal amount) {}

    /**
     * Daily spending trend breakdown for bar chart.
     */
    @GetMapping("/analytics/daily")
    public ResponseEntity<List<DailySpendResponse>> getDailySpending(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }

        LocalDate baseDate = (year != null && month != null)
                ? LocalDate.of(year, month, 1)
                : LocalDate.now();
        LocalDate startOfMonth = baseDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = baseDate.with(TemporalAdjusters.lastDayOfMonth());

        List<Expense> monthlyExpenses = expenseRepository.findByTelegramUserIdAndTransactionDateBetweenAndStatus(
                userId, startOfMonth, endOfMonth, ExpenseStatus.CONFIRMED);

        Map<LocalDate, BigDecimal> dailyMap = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getTransactionDate,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        List<DailySpendResponse> results = new ArrayList<>();
        int daysInMonth = endOfMonth.getDayOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = baseDate.withDayOfMonth(d);
            BigDecimal amount = dailyMap.getOrDefault(date, BigDecimal.ZERO);
            results.add(new DailySpendResponse(date.toString(), d, amount));
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Export all expenses to CSV for current user.
     */
    @GetMapping("/expenses/export")
    public ResponseEntity<String> exportExpensesCsv(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        Long userId = resolveUserId(authHeader, token);
        List<Expense> expenses;
        if (userId == null) {
            expenses = List.of();
        } else if (year != null && month != null) {
            LocalDate baseDate = LocalDate.of(year, month, 1);
            LocalDate start = baseDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = baseDate.with(TemporalAdjusters.lastDayOfMonth());
            expenses = expenseRepository.findByTelegramUserIdAndStatusAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                    userId, ExpenseStatus.CONFIRMED, start, end, PageRequest.of(0, 10000)).getContent();
        } else {
            expenses = expenseRepository.findByTelegramUserIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
                    userId, ExpenseStatus.CONFIRMED, PageRequest.of(0, 10000)).getContent();
        }

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Merchant,Category,Amount,Status,Notes\n");

        for (Expense e : expenses) {
            csv.append(e.getId()).append(",")
               .append(e.getTransactionDate()).append(",")
               .append("\"").append(e.getMerchant() != null ? e.getMerchant().replace("\"", "\"\"") : "").append("\",")
               .append(e.getCategory()).append(",")
               .append(e.getAmount()).append(",")
               .append(e.getStatus()).append(",")
               .append("\"").append(e.getNotes() != null ? e.getNotes().replace("\"", "\"\"") : "").append("\"\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"expenses_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    /**
     * Get profile and budget details for current user.
     */
    @GetMapping("/user/profile")
    public ResponseEntity<UserProfile> getUserProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }

        return userProfileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update user monthly budget from web dashboard.
     * If year and month are provided, updates/creates the budget for that specific month.
     * Otherwise updates the default profile budget.
     */
    @PutMapping("/user/budget")
    public ResponseEntity<?> updateBudget(
            @RequestBody BudgetUpdateRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String token) {

        Long userId = resolveUserId(authHeader, token);
        if (userId == null || request.budget() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (request.year() != null && request.month() != null) {
            MonthlyBudget monthlyBudget = monthlyBudgetRepository
                    .findByTelegramUserIdAndYearAndMonth(userId, request.year(), request.month())
                    .orElseGet(() -> new MonthlyBudget(userId, request.year(), request.month(), request.budget()));
            monthlyBudget.setBudget(request.budget());
            MonthlyBudget saved = monthlyBudgetRepository.save(monthlyBudget);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "year", saved.getYear(),
                    "month", saved.getMonth(),
                    "budget", saved.getBudget()
            ));
        }

        return userProfileRepository.findById(userId)
                .map(profile -> {
                    profile.setMonthlyBudget(request.budget());
                    profile.setLastWarningThreshold(0);
                    return ResponseEntity.ok(userProfileRepository.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void checkAndSendBudgetAlert(Long userId, UserProfile profile) {
        if (profile == null) return;
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
            telegramService.sendMessage(userId, alert);
        } else if (pct >= 80 && profile.getLastWarningThreshold() < 80) {
            profile.setLastWarningThreshold(80);
            userProfileRepository.save(profile);
            String alert = String.format(
                "⚠️ <b>Budget Warning (80%% Reached)!</b>\n\nYou have spent <b>RM %.2f</b> of your <b>RM %.2f</b> monthly budget (%d%% used). Watch your expenses!",
                spent, budget, pct
            );
            telegramService.sendMessage(userId, alert);
        }
    }
}
