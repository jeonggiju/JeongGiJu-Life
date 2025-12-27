package com.study.jeonggiju.domain.numberRecord.entity;

import java.time.LocalDate;
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
public class NumberRecord {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="category_id")
	private Category category;

	@Column
	private double number;

	private LocalDate date;

	protected NumberRecord() {}

	public static NumberRecord of(Category category,double number, LocalDate date) {
		return NumberRecord.builder().category(category).number(number).date(date).build();
	}

	public void update(double number,LocalDate date){
		this.number = number;
		this.date = date;
	}
}
