package com.telereceipt.dto;

import java.math.BigDecimal;

public class AnalyticsSummaryResponse {
    private BigDecimal totalSpentMonth;
    private BigDecimal monthlyBudget;
    private int budgetPercentageUsed;
    private long transactionCount;
    private String topCategory;

    public AnalyticsSummaryResponse() {
    }

    public AnalyticsSummaryResponse(BigDecimal totalSpentMonth, BigDecimal monthlyBudget,
                                    int budgetPercentageUsed, long transactionCount, String topCategory) {
        this.totalSpentMonth = totalSpentMonth;
        this.monthlyBudget = monthlyBudget;
        this.budgetPercentageUsed = budgetPercentageUsed;
        this.transactionCount = transactionCount;
        this.topCategory = topCategory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal totalSpentMonth;
        private BigDecimal monthlyBudget;
        private int budgetPercentageUsed;
        private long transactionCount;
        private String topCategory;

        public Builder totalSpentMonth(BigDecimal totalSpentMonth) { this.totalSpentMonth = totalSpentMonth; return this; }
        public Builder monthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; return this; }
        public Builder budgetPercentageUsed(int budgetPercentageUsed) { this.budgetPercentageUsed = budgetPercentageUsed; return this; }
        public Builder transactionCount(long transactionCount) { this.transactionCount = transactionCount; return this; }
        public Builder topCategory(String topCategory) { this.topCategory = topCategory; return this; }

        public AnalyticsSummaryResponse build() {
            return new AnalyticsSummaryResponse(totalSpentMonth, monthlyBudget, budgetPercentageUsed, transactionCount, topCategory);
        }
    }

    public BigDecimal getTotalSpentMonth() { return totalSpentMonth; }
    public void setTotalSpentMonth(BigDecimal totalSpentMonth) { this.totalSpentMonth = totalSpentMonth; }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public int getBudgetPercentageUsed() { return budgetPercentageUsed; }
    public void setBudgetPercentageUsed(int budgetPercentageUsed) { this.budgetPercentageUsed = budgetPercentageUsed; }

    public long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(long transactionCount) { this.transactionCount = transactionCount; }

    public String getTopCategory() { return topCategory; }
    public void setTopCategory(String topCategory) { this.topCategory = topCategory; }
}
