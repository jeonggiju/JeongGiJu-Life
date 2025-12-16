package com.study.jeonggiju.domain.sleep.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class SleepDto {

	private LocalTime sleepTime;
	private LocalTime wakeTime;
	private LocalDate date;
}
