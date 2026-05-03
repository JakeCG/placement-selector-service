package com.example.placementSelector.controller;

import com.example.placementSelector.model.dto.FilterRequest;
import com.example.placementSelector.model.dto.PlacementResponse;
import com.example.placementSelector.service.PlacementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * REST controller for selecting and exporting YouTube ad placement candidates.
 *
 * <p>Supports filtering videos, exporting results, and resetting the current selection state.</p>
 */
@RestController
@RequestMapping("/api/placements")
@RequiredArgsConstructor
public class PlacementController {
	
	private final PlacementService service;
	
	/**
	 * Filters available videos based on Ad criteria and returns matching placements.
	 *
	 * @param request filter criteria
	 * @return filtered placement response
	 */
	@PostMapping("/filter")
	public PlacementResponse filter(@RequestBody FilterRequest request) {
		return service.filter(request);
	}
	
	/**
	 * Exports the most recently filtered placement results as a CSV file.
	 *
	 * <p>Only CSV format is currently supported. Calling this endpoint before filtering
	 * will result in an error from the service layer.</p>
	 *
	 * @param format export format (must be "csv")
	 * @return CSV file as downloadable response
	 */
	@GetMapping("/export")
	public ResponseEntity<String> export(@RequestParam String format) throws ResponseStatusException {
		
		if (!"csv".equalsIgnoreCase(format)) {
			throw new ResponseStatusException(BAD_REQUEST, "Only CSV supported");
		}
		
		return ResponseEntity.ok()
						.header(CONTENT_DISPOSITION, "attachment; filename=placements.csv")
						.body(service.exportCsv());
	}
	
	/**
	 * Clears the current filtered placement state.
	 *
	 * <p>This allows re-running filters without retaining previous results.</p>
	 *
	 * @return empty 204 response
	 */
	@DeleteMapping("/filter")
	public ResponseEntity<Void> reset() {
		service.reset();
		return ResponseEntity.noContent().build();
	}
}
