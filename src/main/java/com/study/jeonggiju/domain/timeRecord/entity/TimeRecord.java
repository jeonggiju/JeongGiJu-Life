package com.study.jeonggiju.domain.timeRecord.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.study.jeonggiju.domain.category.entity.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Table
@Entity
@Builder
@Data
@AllArgsConstructor
public class TimeRecord {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="category_id")
	private Category category;

	@Column
	private LocalTime time;

	private LocalDate date;

	protected TimeRecord() {}

	public static TimeRecord of(Category category,LocalTime time, LocalDate date) {
		return TimeRecord.builder().category(category).time(time).date(date).build();
	}

	public void update(LocalTime time,LocalDate date){
		this.time = time;
		this.date = date;
	}
}
