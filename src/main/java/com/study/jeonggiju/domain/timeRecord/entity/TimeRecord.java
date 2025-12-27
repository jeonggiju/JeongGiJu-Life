package com.study.jeonggiju.domain.timeRecord.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

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
	@JoinColumn(name="category_id")
	private Category category;

	@Column
	private LocalDate date;

	@CreatedDate
	private LocalDate createdAt;

	protected TimeRecord() {}

	public static TimeRecord of(Category category, LocalDate date) {
		return TimeRecord.builder().category(category).date(date).build();
	}

	public void update(LocalDate date){
		this.date = date;
	}
}
