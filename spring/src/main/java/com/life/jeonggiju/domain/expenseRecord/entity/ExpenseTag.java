package com.life.jeonggiju.domain.expenseRecord.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Table
@Entity
@Builder
@Data
@AllArgsConstructor
public class ExpenseTag {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	private String name;

	protected ExpenseTag() {
	}

	public static ExpenseTag of(String name) {
		return ExpenseTag.builder()
			.name(name)
			.build();
	}

	public void update(String name) {
		this.name = name;
	}
}
