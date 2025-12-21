package com.study.jeonggiju.domain.exercise.dto;


import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ExerciseDto {
	private boolean work;
	private LocalTime time;
	private LocalDate date;
}
