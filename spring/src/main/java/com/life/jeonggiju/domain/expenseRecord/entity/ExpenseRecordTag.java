package com.life.jeonggiju.domain.expenseRecord.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class ExpenseRecordTag {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "expense_record_id")
	@JsonIgnore
	private ExpenseRecord expenseRecord;

	@ManyToOne
	@JoinColumn(name = "expense_tag_id")
	private ExpenseTag expenseTag;

	protected ExpenseRecordTag() {
	}

	public static ExpenseRecordTag of(ExpenseRecord expenseRecord, ExpenseTag expenseTag) {
		return ExpenseRecordTag.builder()
			.expenseRecord(expenseRecord)
			.expenseTag(expenseTag)
			.build();
	}
}
