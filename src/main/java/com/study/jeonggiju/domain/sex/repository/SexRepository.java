package com.study.jeonggiju.domain.sex.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.sex.entity.Sex;

@Repository
public interface SexRepository extends JpaRepository<Sex, Long> {

	Optional<Sex> findByDate(LocalDate date);

}
