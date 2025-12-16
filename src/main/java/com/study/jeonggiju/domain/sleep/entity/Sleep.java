package com.study.jeonggiju.domain.sleep.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;
import java.time.LocalTime;

import com.study.jeonggiju.domain.sleep.dto.SleepDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Sleep {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	private LocalTime sleepTime;
	private LocalTime wakeTime;
	private LocalDate date;

	public static Sleep from(SleepDto dto) {
		return Sleep.builder().sleepTime(dto.getSleepTime()).wakeTime(dto.getWakeTime()).date(dto.getDate()).build();
	}

	public void update(SleepDto dto) {
		this.sleepTime = dto.getSleepTime();
		this.wakeTime = dto.getWakeTime();
	}
}
