package com.study.jeonggiju.domain.checkRecord.entity;

import java.time.LocalDate;
import java.util.List;
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
public class CheckRecord {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category;

	private boolean success;

	@CreatedDate
	private LocalDate createdAt;

	protected CheckRecord() {}

	public static CheckRecord of(Category category, boolean success) {
		return CheckRecord.builder().category(category).success(success).build();
	}

	public void update(boolean success){
		this.success = success;
	}
}

