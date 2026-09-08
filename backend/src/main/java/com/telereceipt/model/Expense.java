package com.telereceipt.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expense_user_date", columnList = "telegramUserId, transactionDate"),
    @Index(name = "idx_expense_category", columnList = "category")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(length = 150)
    private String merchant;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExpenseStatus status;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String receiptImagePath;

    @Column(length = 255)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Expense() {
    }

    public Expense(Long id, Long telegramUserId, String merchant, BigDecimal amount,
                   ExpenseCategory category, ExpenseStatus status, LocalDate transactionDate,
                   String receiptImagePath, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.telegramUserId = telegramUserId;
        this.merchant = merchant;
        this.amount = amount;
        this.category = category;
        this.status = status;
        this.transactionDate = transactionDate;
        this.receiptImagePath = receiptImagePath;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = ExpenseStatus.CONFIRMED;
        }
    }

    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long telegramUserId;
        private String merchant;
        private BigDecimal amount;
        private ExpenseCategory category;
        private ExpenseStatus status;
        private LocalDate transactionDate;
        private String receiptImagePath;
        private String notes;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder telegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; return this; }
        public Builder merchant(String merchant) { this.merchant = merchant; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder category(ExpenseCategory category) { this.category = category; return this; }
        public Builder status(ExpenseStatus status) { this.status = status; return this; }
        public Builder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public Builder receiptImagePath(String receiptImagePath) { this.receiptImagePath = receiptImagePath; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Expense build() {
            return new Expense(id, telegramUserId, merchant, amount, category, status, transactionDate, receiptImagePath, notes, createdAt);
        }
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }

    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getReceiptImagePath() { return receiptImagePath; }
    public void setReceiptImagePath(String receiptImagePath) { this.receiptImagePath = receiptImagePath; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
