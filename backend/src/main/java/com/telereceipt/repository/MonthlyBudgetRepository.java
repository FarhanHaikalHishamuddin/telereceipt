package com.telereceipt.repository;

import com.telereceipt.model.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByTelegramUserIdAndYearAndMonth(Long telegramUserId, Integer year, Integer month);
}
