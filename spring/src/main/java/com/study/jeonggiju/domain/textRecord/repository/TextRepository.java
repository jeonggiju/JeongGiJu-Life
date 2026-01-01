package com.study.jeonggiju.domain.textRecord.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.textRecord.entity.TextRecord;

public interface TextRepository extends JpaRepository<TextRecord, UUID> {

	List<TextRecord> findAllByCategory_Id(UUID categoryId);
	Optional<List<TextRecord>> findByCategoryIdAndDate(UUID categoryId, LocalDate date);

}
