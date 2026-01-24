package com.life.jeonggiju.domain.textRecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.textRecord.entity.TextRecordImage;

public interface TextRecordImageRepository extends JpaRepository<TextRecordImage, UUID> {

	List<TextRecordImage> findByTextRecordIdOrderByDisplayOrderAsc(UUID textRecordId);

	int countByTextRecordId(UUID textRecordId);
}
