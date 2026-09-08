package com.telereceipt.dto;

import com.telereceipt.model.ExpenseCategory;

import java.math.BigDecimal;

public class CategorySummaryResponse {
    private ExpenseCategory category;
    private BigDecimal totalAmount;

    public CategorySummaryResponse() {
    }

    public CategorySummaryResponse(ExpenseCategory category, BigDecimal totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
