package com.study.jeonggiju.domain.category.entity;

import java.util.UUID;

import com.study.jeonggiju.domain.user.entity.User;

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
public class Category {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;


	@Column(columnDefinition = "TEXT")
	private String description;

	private RecordType recordType;

	private String title;

	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;

	protected Category() {
	}
}
