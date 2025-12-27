package com.study.jeonggiju.domain.checkRecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.checkRecord.entity.CheckRecord;

public interface CheckRepository extends JpaRepository<CheckRecord, UUID> {

	List<CheckRecord> findAllByCategory_Id(UUID categoryId);
}
