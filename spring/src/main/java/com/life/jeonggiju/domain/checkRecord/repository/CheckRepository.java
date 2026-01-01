package com.life.jeonggiju.domain.checkRecord.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.checkRecord.entity.CheckRecord;

public interface CheckRepository extends JpaRepository<CheckRecord, UUID> {

	List<CheckRecord> findAllByCategory_Id(UUID categoryId);

	Optional<CheckRecord> findByCategoryIdAndDate(UUID categoryId, LocalDate date);
}
