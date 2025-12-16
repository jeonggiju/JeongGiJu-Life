package com.study.jeonggiju.domain.smoking.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.study.jeonggiju.domain.sleep.entity.Sleep;
import com.study.jeonggiju.domain.smoking.entity.Smoking;

public interface SmokingRepository extends JpaRepository<Smoking, Long> {


	Optional<Smoking> findByDate(LocalDate date);
	void deleteByDate(LocalDate date);
}
