package com.study.jeonggiju.domain.category.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.study.jeonggiju.domain.checkRecord.entity.CheckRecord;
import com.study.jeonggiju.domain.textRecord.entity.TextRecord;
import com.study.jeonggiju.domain.timeRecord.entity.TimeRecord;
import com.study.jeonggiju.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import lombok.ToString;

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

	@Enumerated(EnumType.STRING)
	private RecordType recordType;

	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Visibility visibility;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="user_id")
	@ToString.Exclude
	private User user;

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	@ToString.Exclude
	private List<TextRecord> textRecords = new ArrayList<>();

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	@ToString.Exclude
	private List<CheckRecord> checkRecords = new ArrayList<>();

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	@ToString.Exclude
	private List<TimeRecord> timeRecords = new ArrayList<>();

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	public List<CategoryLike> categoryLike;

	protected Category() {
	}

	public static Category of(String title, String description, RecordType recordType, User user, Visibility visibility) {
		return Category.builder().title(title).description(description).recordType(recordType).user(user).visibility(visibility).build();
	}

	public void update(String title, String description, Visibility visibility){
		this.title = title;
		this.description = description;
		this.visibility = visibility;
	}
}
