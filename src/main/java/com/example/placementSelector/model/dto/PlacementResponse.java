package com.example.placementSelector.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response object returned after filtering video placements.
 *
 * <p>Contains both the total available candidates and the subset
 * that matched the applied filter criteria.</p>
 */
@Data
@AllArgsConstructor
public class PlacementResponse {
	private int totalCandidates;
	private int matchedCount;
	private List<VideoDto> videos;
}
