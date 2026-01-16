package com.life.jeonggiju.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SortSpec {
	PublicCategorySortKey key;
	SortDir dir;
}
