package com.life.jeonggiju.domain.expenseRecord.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseRecord;

public interface ExpenseRepository extends JpaRepository<ExpenseRecord, UUID> {

	List<ExpenseRecord> findAllByCategory_Id(UUID categoryId);

	Optional<List<ExpenseRecord>> findByCategoryIdAndDate(UUID categoryId, LocalDate date);
}
