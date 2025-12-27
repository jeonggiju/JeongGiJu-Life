package com.study.jeonggiju.domain.textRecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.textRecord.entity.TextRecord;

public interface TextRepository extends JpaRepository<TextRecord, UUID> {

	List<TextRecord> findAllByCategoryId(UUID categoryId);
}
