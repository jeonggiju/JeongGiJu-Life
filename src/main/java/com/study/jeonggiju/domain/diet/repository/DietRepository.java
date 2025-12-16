package com.study.jeonggiju.domain.diet.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.diet.entity.Diet;

@Repository
public interface DietRepository extends JpaRepository<Diet, Long> {

	Optional<Diet> findByDate(LocalDate date);

	void deleteByDate(LocalDate date);
}
