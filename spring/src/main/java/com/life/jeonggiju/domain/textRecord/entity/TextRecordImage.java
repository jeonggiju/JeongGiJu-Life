package com.life.jeonggiju.domain.textRecord.entity;

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
import lombok.NoArgsConstructor;

@Table
@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextRecordImage {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "text_record_id", nullable = false)
	@JsonIgnore
	private TextRecord textRecord;

	private String imageUrl;

	private int displayOrder;

	public static TextRecordImage of(TextRecord textRecord, String imageUrl, int displayOrder) {
		return TextRecordImage.builder()
			.textRecord(textRecord)
			.imageUrl(imageUrl)
			.displayOrder(displayOrder)
			.build();
	}
}
