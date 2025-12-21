package com.study.jeonggiju.domain.exercise.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;
import java.time.LocalTime;

import com.study.jeonggiju.domain.exercise.dto.ExerciseDto;

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
@Table(name = "exercise", uniqueConstraints = {
	@UniqueConstraint(columnNames = "date")
})
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Exercise {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private boolean work;
	private LocalTime time;

	@Column(nullable = false, unique = true)
	private LocalDate date;

	public static Exercise from(ExerciseDto dto) {
		return Exercise.builder().work(dto.isWork()).date(dto.getDate()).time(dto.getTime()).build();
	}

	public void update(ExerciseDto dto) {
		this.work = dto.isWork();
		this.time =  dto.getTime();
	}
}
