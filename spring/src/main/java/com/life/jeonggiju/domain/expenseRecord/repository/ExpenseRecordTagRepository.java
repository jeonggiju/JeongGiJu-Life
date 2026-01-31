package com.life.jeonggiju.domain.expenseRecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseRecordTag;

public interface ExpenseRecordTagRepository extends JpaRepository<ExpenseRecordTag, UUID> {

	List<ExpenseRecordTag> findAllByExpenseRecord_Id(UUID expenseRecordId);

	List<ExpenseRecordTag> findAllByExpenseTag_Id(UUID expenseTagId);

	void deleteAllByExpenseTag_Id(UUID expenseTagId);
}
