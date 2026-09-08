package com.telereceipt.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_year_month", columnNames = {"telegramUserId", "budget_year", "budget_month"})
})
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(name = "budget_year", nullable = false)
    private Integer year;

    @Column(name = "budget_month", nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public MonthlyBudget() {
    }

    public MonthlyBudget(Long telegramUserId, Integer year, Integer month, BigDecimal budget) {
        this.telegramUserId = telegramUserId;
        this.year = year;
        this.month = month;
        this.budget = budget;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
