package com.study.jeonggiju.domain.timeRecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.timeRecord.entity.TimeRecord;

public interface TimeRepository extends JpaRepository<TimeRecord, UUID> {
	List<TimeRecord> findAllByCategoryId(UUID categoryId);
}
