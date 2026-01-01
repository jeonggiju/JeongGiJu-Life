package com.study.jeonggiju.domain.user.entity;

import java.util.List;
import java.util.UUID;

import com.study.jeonggiju.domain.category.entity.Category;
import com.study.jeonggiju.domain.category.entity.CategoryLike;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "users")
@Builder @Data
@AllArgsConstructor
@ToString(exclude = "categories")
public class User {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@Column(unique = true)
	private String email;

	@Column
	private String username;

	@Column(columnDefinition = "TEXT")
	private String password;

	@Column
	private String title;

	@Column
	private String description;

	@Column
	private Authority authority;

	@Column
	private int birthYear;

	@Column
	private int birthMonth;

	@Column
	private int birthDay;

	@OneToMany(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<Category> categories;

	@OneToMany(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<CategoryLike> categoryLike;

	protected User() {
	}

	public void update(String title, String description){
		this.title = title;
		this.description = description;
	}
}
