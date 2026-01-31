package com.life.jeonggiju.domain.expenseRecord.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.life.jeonggiju.domain.category.entity.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Table
@Entity
@Builder
@Data
@AllArgsConstructor
public class ExpenseRecord {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "category_id")
	@JsonIgnore
	private Category category;

	private double amount;

	@OneToMany(mappedBy = "expenseRecord", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ExpenseRecordTag> tags = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private ExpenseType expenseType;

	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;

	private String merchant;

	private String memo;

	private LocalDate date;

	protected ExpenseRecord() {
	}

	public static ExpenseRecord of(Category category, double amount, ExpenseType expenseType,
		PaymentMethod paymentMethod, String merchant, String memo, LocalDate date) {
		return ExpenseRecord.builder()
			.category(category)
			.amount(amount)
			.expenseType(expenseType)
			.paymentMethod(paymentMethod)
			.merchant(merchant)
			.memo(memo)
			.date(date)
			.build();
	}

	public void update(double amount, ExpenseType expenseType, PaymentMethod paymentMethod,
		String merchant, String memo, LocalDate date) {
		this.amount = amount;
		this.expenseType = expenseType;
		this.paymentMethod = paymentMethod;
		this.merchant = merchant;
		this.memo = memo;
		this.date = date;
	}

	public void addTag(ExpenseTag tag) {
		ExpenseRecordTag recordTag = ExpenseRecordTag.of(this, tag);
		this.tags.add(recordTag);
	}

	public void clearTags() {
		this.tags.clear();
	}
}
