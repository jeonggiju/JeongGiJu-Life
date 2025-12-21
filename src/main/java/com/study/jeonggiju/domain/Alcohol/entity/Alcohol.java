package com.study.jeonggiju.domain.Alcohol.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;

import com.study.jeonggiju.domain.Alcohol.dto.AlcoholDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alcohol", uniqueConstraints = {
	@UniqueConstraint(columnNames = "date")
})
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Alcohol {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private boolean drink;
	@Column(nullable = false, unique = true)
	private LocalDate date;

	public static Alcohol from(AlcoholDto dto) {
		return Alcohol.builder().drink(dto.isDrink()).date(dto.getDate()).build();
	}

	public void update(AlcoholDto dto) {
		this.drink = dto.isDrink();
	}
}
