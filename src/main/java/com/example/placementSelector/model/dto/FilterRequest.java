package com.example.placementSelector.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Request object used to filter YouTube video placements.
 *
 * <p>All fields are optional. Only provided values are applied as filters.</p>
 *
 * <p>Filters operate across both video-level and channel-level attributes.</p>
 */
@Data
public class FilterRequest {
	private Long minViewCount;
	private Long minLikeCount;
	private Long minSubscriberCount;
	private List<String> languages;
	private Boolean excludeMadeForKids;
	private Boolean requireEmbeddable;
	private LocalDate publishedAfter;
	private Integer minDurationSeconds;
	private Integer maxDurationSeconds;
}
