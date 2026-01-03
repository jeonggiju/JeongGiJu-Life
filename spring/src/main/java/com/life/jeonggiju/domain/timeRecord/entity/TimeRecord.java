package com.life.jeonggiju.domain.timeRecord.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Table(uniqueConstraints = @UniqueConstraint(name = "uk_time_record_category_date",columnNames = {"category_id", "date"}))
@Entity
@Builder
@Data
@AllArgsConstructor
public class TimeRecord extends BaseEntity {

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
