package com.study.jeonggiju.domain.sleep.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.caffiene.entity.Caffeine;
import com.study.jeonggiju.domain.sleep.entity.Sleep;

@Repository
public interface SleepRepository extends JpaRepository<Sleep, Long> {

	Optional<Sleep> findByDate(LocalDate date);

}
