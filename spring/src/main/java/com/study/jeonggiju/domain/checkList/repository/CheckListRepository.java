package com.study.jeonggiju.domain.checkList.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.checkList.entity.CheckListRecord;

public interface CheckListRepository extends JpaRepository<CheckListRecord, UUID> {

	List<CheckListRecord> findAllByCategory_Id(UUID categoryId);
	Optional<List<CheckListRecord>> findByCategoryIdAndDate(UUID categoryId, LocalDate date);
}
