package com.life.jeonggiju.domain.checkRecord.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Table(uniqueConstraints = @UniqueConstraint(name = "uk_category_date",columnNames = {"category_id", "date"}))
@Entity
@Builder
@Data
@AllArgsConstructor
public class CheckRecord extends BaseEntity {

	@ManyToOne
	@JoinColumn(name="category_id")
	@JsonIgnore
	private Category category;

	private boolean success;

	private LocalDate date;

	protected CheckRecord() {}

	public static CheckRecord of(Category category, boolean success, LocalDate date) {
		return CheckRecord.builder().category(category).success(success).date(date).build();
	}

	public void update(boolean success, LocalDate date){
		this.success = success;
		this.date = date;
	}
}

