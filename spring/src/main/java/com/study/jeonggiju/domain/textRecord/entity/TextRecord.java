package com.study.jeonggiju.domain.textRecord.entity;

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
public class TextRecord {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name="category_id", nullable = false)
	@JsonIgnore
	private Category category;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String text;

	private LocalDate date;

	protected TextRecord() {}

	public static TextRecord of(Category category, String title, String text, LocalDate date) {
		return TextRecord.builder().category(category).title(title).text(text).date(date).build();
	}

	public void update(String title, String text, LocalDate date){
		this.title = title;
		this.text = text;
		this.date = date;
	}
}
