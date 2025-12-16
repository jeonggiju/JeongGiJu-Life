package com.study.jeonggiju.domain.diet.dto;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class DietDto {
	private boolean drink;
	private LocalDate date;
}
