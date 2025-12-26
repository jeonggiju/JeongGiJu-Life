package com.study.jeonggiju.domain.category.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.study.jeonggiju.domain.checkRecord.entity.CheckRecord;
import com.study.jeonggiju.domain.textRecord.entity.TextRecord;
import com.study.jeonggiju.domain.timeRecord.entity.TimeRecord;
import com.study.jeonggiju.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<TextRecord> textRecords = new ArrayList<>();

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<CheckRecord> checkRecords = new ArrayList<>();

	@OneToMany(
		mappedBy = "category",
		cascade = CascadeType.ALL,
		orphanRemoval = true
	)
	private List<TimeRecord> timeRecords = new ArrayList<>();


	protected Category() {
	}

	public static Category of(String title, String description, RecordType recordType, User user) {
		return Category.builder().title(title).description(description).recordType(recordType).user(user).build();
	}

	public void update(String title, String description){
		this.title = title;
		this.description = description;
	}
}
