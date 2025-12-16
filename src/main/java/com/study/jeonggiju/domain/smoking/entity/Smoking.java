package com.study.jeonggiju.domain.smoking.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDate;

import com.study.jeonggiju.domain.smoking.dto.SmokingDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity @Table
public class Smoking {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	private Long id;
	private Boolean smoke;
	private LocalDate date;

	public static Smoking from(SmokingDto dto){
		return Smoking.builder().smoke(dto.getSmoke()).date(dto.getDate()).build();
	}

	public void update(SmokingDto dto){
		this.smoke = dto.getSmoke();
	}
}
