package com.study.jeonggiju.domain.sex.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;
import java.time.LocalTime;

import com.study.jeonggiju.domain.sex.dto.SexDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sex", uniqueConstraints = {
	@UniqueConstraint(columnNames = "date")
})
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class Sex {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private boolean sex;

	@Column(nullable = false, unique = true)
	private LocalDate date;

	public static Sex from(SexDto dto) {
		return Sex.builder().sex(dto.isSex()).date(dto.getDate()).build();
	}

	public void update(SexDto dto) {
		this.sex = dto.isSex();
	}
}
