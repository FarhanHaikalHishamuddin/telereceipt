package com.telereceipt.service;

import com.telereceipt.model.ExpenseCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextExpenseParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:rm\\s*)?(\\d+(?:\\.\\d{1,2})?)"
    );

    public record ParsedTextExpense(
            boolean valid,
            BigDecimal amount,
            ExpenseCategory category,
            String merchant,
            String notes
    ) {}

    public ParsedTextExpense parse(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ParsedTextExpense(false, null, null, null, null);
        }

        String text = rawText.trim();
        Matcher matcher = AMOUNT_PATTERN.matcher(text);

        if (!matcher.find()) {
            return new ParsedTextExpense(false, null, null, null, null);
        }

        String amountString = matcher.group(1);
        BigDecimal amount = new BigDecimal(amountString).setScale(2, RoundingMode.HALF_UP);

        // Remove the amount from the text to get remaining description
        String remainder = text.replaceFirst(Pattern.quote(matcher.group(0)), "").trim();
        if (remainder.isEmpty()) {
            remainder = "Expense";
        }

        ExpenseCategory category = inferCategory(remainder);

        return new ParsedTextExpense(
                true,
                amount,
                category,
                remainder,
                remainder
        );
    }

    private ExpenseCategory inferCategory(String text) {
        String lower = text.toLowerCase();

        // Food & Beverage
        if (lower.matches(".*\\b(lunch|dinner|breakfast|makan|kopi|coffee|nasi|cafe|food|teh|roti|ayam|kfc|mcd|starbucks|burger|bakery|kenangan)\\b.*")) {
            return ExpenseCategory.FOOD;
        }

        // Transport
        if (lower.matches(".*\\b(grab|petrol|fuel|ron95|ron97|parking|toll|touchngo|tng|lrt|mrt|bus|diesel|flight|airasia)\\b.*")) {
            return ExpenseCategory.TRANSPORT;
        }

        // Groceries
        if (lower.matches(".*\\b(groceries|lotus|jaya|giant|aeon|market|pasar|speedmart|99|hero|econsave|village grocer)\\b.*")) {
            return ExpenseCategory.GROCERIES;
        }

        // Utilities
        if (lower.matches(".*\\b(bill|electricity|tnb|water|air|wifi|unifi|celcom|maxis|digi|umobile|telekom|indah water)\\b.*")) {
            return ExpenseCategory.UTILITIES;
        }

        // Shopping
        if (lower.matches(".*\\b(shopee|lazada|uniqlo|zara|padini|watsons|guardian|mr diy|shoes|shirt|clothes)\\b.*")) {
            return ExpenseCategory.SHOPPING;
        }

        // Entertainment
        if (lower.matches(".*\\b(cinema|movie|gsc|tgv|netflix|spotify|steam|game|playstation)\\b.*")) {
            return ExpenseCategory.ENTERTAINMENT;
        }

        return ExpenseCategory.OTHER;
    }
}
