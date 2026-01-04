package com.life.jeonggiju.domain.timeRecord.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.timeRecord.entity.TimeRecord;

public interface TimeRepository extends JpaRepository<TimeRecord, UUID> {
	List<TimeRecord> findAllByCategory_Id(UUID categoryId);

	Optional<TimeRecord> findByCategoryIdAndDate(UUID categoryId, LocalDate date);

}
