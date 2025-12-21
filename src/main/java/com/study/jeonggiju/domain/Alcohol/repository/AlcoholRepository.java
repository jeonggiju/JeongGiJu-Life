package com.study.jeonggiju.domain.Alcohol.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.study.jeonggiju.domain.Alcohol.entity.Alcohol;

@Repository
public interface AlcoholRepository extends JpaRepository<Alcohol, Long> {

	Optional<Alcohol> findByDate(LocalDate date);

}
