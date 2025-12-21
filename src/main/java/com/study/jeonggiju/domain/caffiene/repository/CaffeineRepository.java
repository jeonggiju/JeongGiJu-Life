package com.study.jeonggiju.domain.caffiene.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.caffiene.entity.Caffeine;

@Repository
public interface CaffeineRepository extends JpaRepository<Caffeine, Long> {

	Optional<Caffeine> findByDate(LocalDate date);

}
