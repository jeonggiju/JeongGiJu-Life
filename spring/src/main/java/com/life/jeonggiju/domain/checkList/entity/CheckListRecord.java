package com.life.jeonggiju.domain.checkList.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.common.entity.BaseEntity;

import jakarta.persistence.Entity;
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
public class CheckListRecord extends BaseEntity {

	@ManyToOne
	@JoinColumn(name="category_id")
	@JsonIgnore
	private Category category;

	private String todo;
	private boolean success;

	private LocalDate date;

	protected CheckListRecord() {}

	public static CheckListRecord of(Category category, String todo,boolean success, LocalDate date) {
		return CheckListRecord.builder().category(category).todo(todo).success(success).date(date).build();
	}

	public void update(String todo, boolean success, LocalDate date){
		this.success = success;
		this.date = date;
	}
}

