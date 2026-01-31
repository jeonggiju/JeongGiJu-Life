package com.life.jeonggiju.domain.expenseRecord.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.expenseRecord.entity.ExpenseTag;

public interface ExpenseTagRepository extends JpaRepository<ExpenseTag, UUID> {
}
