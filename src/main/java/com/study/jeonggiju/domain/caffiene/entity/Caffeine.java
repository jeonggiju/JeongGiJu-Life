package com.study.jeonggiju.domain.caffiene.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;

import com.study.jeonggiju.domain.caffiene.dto.CaffeineDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Caffeine {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private boolean drink;
	private LocalDate date;

	public static Caffeine from(CaffeineDto dto) {
		return Caffeine.builder().drink(dto.isDrink()).date(dto.getDate()).build();
	}

	public void update(CaffeineDto dto) {
		this.drink = dto.isDrink();
	}
}
