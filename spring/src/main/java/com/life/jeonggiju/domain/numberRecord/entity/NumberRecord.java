package com.life.jeonggiju.domain.numberRecord.entity;

import java.time.LocalDate;
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

@Table(uniqueConstraints = @UniqueConstraint(name = "uk_number_record_category_date",columnNames = {"category_id", "date"}))
@Entity
@Builder
@Data
@AllArgsConstructor
public class NumberRecord extends BaseEntity {

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
