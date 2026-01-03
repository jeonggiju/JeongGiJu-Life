package com.life.jeonggiju.domain.user.entity;

import java.util.List;

import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.entity.CategoryLike;
import com.life.jeonggiju.domain.category.entity.Comment;
import com.life.jeonggiju.domain.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_email",columnNames = "email"))
@Builder @Data
@AllArgsConstructor
@ToString(exclude = "categories")
public class User extends BaseEntity {

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

	@OneToMany(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<Comment> comments;

	protected User() {
	}

	public void update(String title, String description){
		this.title = title;
		this.description = description;
	}
}
