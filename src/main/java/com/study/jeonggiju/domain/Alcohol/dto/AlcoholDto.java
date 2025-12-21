package com.study.jeonggiju.domain.Alcohol.dto;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class AlcoholDto {
	private boolean drink;
	private LocalDate date;
}
