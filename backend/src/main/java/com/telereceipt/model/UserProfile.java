package com.telereceipt.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private Long telegramUserId;

    @Column(length = 100)
    private String username;

    @Column(length = 100)
    private String firstName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyBudget;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false)
    private Integer lastWarningThreshold; // 0, 80, or 100 to prevent duplicate alerts

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UserProfile() {
    }

    public UserProfile(Long telegramUserId, String username, String firstName,
                       BigDecimal monthlyBudget, String currency, Integer lastWarningThreshold) {
        this.telegramUserId = telegramUserId;
        this.username = username;
        this.firstName = firstName;
        this.monthlyBudget = monthlyBudget != null ? monthlyBudget : new BigDecimal("2000.00");
        this.currency = currency != null ? currency : "MYR";
        this.lastWarningThreshold = lastWarningThreshold != null ? lastWarningThreshold : 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.monthlyBudget == null) {
            this.monthlyBudget = new BigDecimal("2000.00");
        }
        if (this.currency == null) {
            this.currency = "MYR";
        }
        if (this.lastWarningThreshold == null) {
            this.lastWarningThreshold = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Standard Getters and Setters
    public Long getTelegramUserId() { return telegramUserId; }
    public void setTelegramUserId(Long telegramUserId) { this.telegramUserId = telegramUserId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(BigDecimal monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getLastWarningThreshold() { return lastWarningThreshold; }
    public void setLastWarningThreshold(Integer lastWarningThreshold) { this.lastWarningThreshold = lastWarningThreshold; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
