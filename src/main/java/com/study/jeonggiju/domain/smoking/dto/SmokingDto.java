package com.study.jeonggiju.domain.smoking.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class SmokingDto {

	private Boolean smoke;
	private LocalDate date;
}
