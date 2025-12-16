package com.study.jeonggiju.domain.diet.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;

import com.study.jeonggiju.domain.diet.dto.DietDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table
@Entity
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Diet {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private boolean drink;
	private LocalDate date;

	public static Diet from(DietDto dto) {
		return Diet.builder().drink(dto.isDrink()).date(dto.getDate()).build();
	}

	public void update(DietDto dto) {
		this.drink = dto.isDrink();
	}
}
