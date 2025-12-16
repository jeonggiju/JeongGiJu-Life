package com.study.jeonggiju.domain.caffiene.dto;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class CaffeineDto {
	private boolean drink;
	private LocalDate date;
}
