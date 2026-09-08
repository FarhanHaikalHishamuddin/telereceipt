package com.telereceipt.repository;

import com.telereceipt.model.Expense;
import com.telereceipt.model.ExpenseCategory;
import com.telereceipt.model.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByTelegramUserIdOrderByTransactionDateDescCreatedAtDesc(
            Long telegramUserId, Pageable pageable);

    Page<Expense> findByTelegramUserIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
            Long telegramUserId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByTelegramUserIdAndStatusAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            Long telegramUserId, ExpenseStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Expense> findByTelegramUserIdAndTransactionDateBetweenAndStatus(
            Long telegramUserId, LocalDate startDate, LocalDate endDate, ExpenseStatus status);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.telegramUserId = :userId AND e.status = :status AND e.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ExpenseStatus status);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.telegramUserId = :userId AND e.status = :status AND e.transactionDate BETWEEN :startDate AND :endDate GROUP BY e.category")
    List<Object[]> sumAmountByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ExpenseStatus status);
}
