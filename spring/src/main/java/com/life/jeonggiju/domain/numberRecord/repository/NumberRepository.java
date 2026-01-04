package com.life.jeonggiju.domain.numberRecord.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.life.jeonggiju.domain.numberRecord.entity.NumberRecord;

public interface NumberRepository extends JpaRepository<NumberRecord, UUID> {
	List<NumberRecord> findAllByCategory_Id(UUID categoryId);

	Optional<NumberRecord> findByCategoryIdAndDate(UUID categoryId, LocalDate date);

}
