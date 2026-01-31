package com.life.jeonggiju.domain.expenseRecord.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.life.jeonggiju.domain.user.entity.User;

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
public class ExpenseTag {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnore
	private User user;

	private String name;

	protected ExpenseTag() {
	}

	public static ExpenseTag of(User user, String name) {
		return ExpenseTag.builder()
			.user(user)
			.name(name)
			.build();
	}

	public void update(String name) {
		this.name = name;
	}
}
