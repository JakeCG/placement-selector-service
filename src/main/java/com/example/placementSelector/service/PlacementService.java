package com.example.placementSelector.service;

import com.example.placementSelector.model.dto.FilterRequest;
import com.example.placementSelector.model.dto.PlacementResponse;
import com.example.placementSelector.model.dto.VideoDto;
import com.example.placementSelector.model.entity.Channel;
import com.example.placementSelector.model.entity.Video;
import com.example.placementSelector.repository.DataRepository;
import com.example.placementSelector.service.export.PlacementCsvExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Service responsible for filtering YouTube videos based on Ad Ops criteria
 * and exporting selected placements as CSV.
 *
 * <p>Core responsibilities:
 * <ul>
 *     <li>Apply multi-criteria filtering on videos</li>
 *     <li>Join video data with channel metadata</li>
 *     <li>Store last filtered result for export</li>
 * </ul>
 *
 * NOTE: This implementation uses in-memory state for simplicity.
 * In production, this would be replaced by a persistent or cache-based approach.
 */
@Service
@RequiredArgsConstructor
public class PlacementService {
	
	private final DataRepository repo;
	
	private final PlacementCsvExporter csvExporter;
	
	// Stores the most recent filter result for export
	private List<Video> lastFilterResult;
	
	private static final String FILTER_NOT_RUN =
			"No placement data available. Please run POST /api/placements/filter first.";
	
	private static final String INVALID_DURATION =
			"Invalid filter: minDurationSeconds must be <= maxDurationSeconds.";
	
	private static final String CSV_HEADER =
			"videoId,channelName,title,viewCount,likeCount,language,durationSeconds\n";
	
	private static final String UNKNOWN_CHANNEL = "Unknown";
	private static final String COMMA = ",";
	
	/**
	 * Filters videos based on the provided criteria.
	 *
	 * @param req filter criteria (all fields optional)
	 * @return filtered and mapped placement response
	 */
	public PlacementResponse filter(FilterRequest req) {
		
		validate(req);
		
		Map<String, Channel> channels = repo.getChannelMap();
		
		List<Video> filtered = repo.getVideos().stream()
										.filter(v -> matchesViewCount(v, req))
										.filter(v -> matchesLikeCount(v, req))
										.filter(v -> matchesLanguage(v, req))
										.filter(v -> matchesMadeForKids(v, req))
										.filter(v -> matchesEmbeddable(v, req))
										.filter(v -> matchesDuration(v, req))
										.filter(v -> matchesPublishedDate(v, req))
										.filter(v -> matchesSubscriberCount(v, req, channels))
										.sorted(Comparator.comparingLong(Video::getViewCount).reversed())
										.toList();
		
		lastFilterResult = filtered;
		
		return mapToResponse(filtered, channels);
	}
	
	/**
	 * Exports the most recently filtered placement results as a CSV string.
	 *
	 * <p>This method retrieves channel metadata and delegates CSV formatting
	 * to {@code PlacementCsvExporter}.</p>
	 *
	 * @return CSV string containing placement data
	 * @throws ResponseStatusException if no filter has been executed yet
	 */
	public String exportCsv() {
		
		if (lastFilterResult == null) {
			throw new ResponseStatusException(CONFLICT, FILTER_NOT_RUN);
		}
		
		Map<String, Channel> channels = repo.getChannelMap();
		
		return csvExporter.export(lastFilterResult, channels);
	}
	
	/**
	 * Resets internal state (clears last filtered result).
	 * Used for testing.
	 */
	public void reset() {
		lastFilterResult = null;
	}
	
	// Filter rules
	
	/**
	 * Checks view count threshold.
	 */
	private boolean matchesViewCount(Video v, FilterRequest req) {
		return req.getMinViewCount() == null || v.getViewCount() >= req.getMinViewCount();
	}
	
	/**
	 * Checks like count threshold.
	 */
	private boolean matchesLikeCount(Video v, FilterRequest req) {
		return req.getMinLikeCount() == null || v.getLikeCount() >= req.getMinLikeCount();
	}
	
	/**
	 * Checks allowed languages.
	 */
	private boolean matchesLanguage(Video v, FilterRequest req) {
		return req.getLanguages() == null || req.getLanguages().contains(v.getLanguage());
	}
	
	/**
	 * Filters out videos that are marked as "made for kids" when the request
	 * explicitly excludes kids content.
	 *
	 * <p>If {@code excludeMadeForKids = true}, only videos that are not marked
	 * as made-for-kids are allowed. If the flag is not set, all videos pass.</p>
	 */
	private boolean matchesMadeForKids(Video v, FilterRequest req) {
		
		if (Boolean.TRUE.equals(req.getExcludeMadeForKids())) {
			return !Boolean.TRUE.equals(v.getIsMadeForKids());
		}
		
		return true;
	}
	
	/**
	 * Filters videos based on embeddability requirements.
	 *
	 * <p>If {@code requireEmbeddable = true}, only videos explicitly marked
	 * as embeddable are included. If the flag is not set, embeddability is ignored.</p>
	 */
	private boolean matchesEmbeddable(Video v, FilterRequest req) {
		
		if (Boolean.TRUE.equals(req.getRequireEmbeddable())) {
			return Boolean.TRUE.equals(v.getIsEmbeddable());
		}
		
		return true;
	}
	
	/**
	 * Checks duration range constraints.
	 */
	private boolean matchesDuration(Video v, FilterRequest req) {
		return (req.getMinDurationSeconds() == null || v.getDurationSeconds() >= req.getMinDurationSeconds())
					&& (req.getMaxDurationSeconds() == null || v.getDurationSeconds() <= req.getMaxDurationSeconds());
	}
	
	/**
	 * Filters videos published after a given date.
	 */
	private boolean matchesPublishedDate(Video v, FilterRequest req) {
		if (req.getPublishedAfter() == null) return true;
		
		return v.getPublishedAt().isAfter(
				req.getPublishedAfter()
						.atStartOfDay()
						.toInstant(UTC)
		);
	}
	
	/**
	 * Checks channel subscriber threshold.
	 */
	private boolean matchesSubscriberCount(Video v,
											FilterRequest req,
											Map<String, Channel> channels) {
		
		if (req.getMinSubscriberCount() == null) return true;
		
		Channel c = channels.get(v.getChannelId());
		
		return c != null && c.getSubscriberCount() >= req.getMinSubscriberCount();
	}
	
	/**
	 * Converts filtered videos into API response DTO.
	 */
	private PlacementResponse mapToResponse(List<Video> videos,
											Map<String, Channel> channels) {
		
		List<VideoDto> dtoList = videos.stream()
										 .map(v -> toDto(v, channels))
										.toList();
		
		return new PlacementResponse(
				repo.getVideos().size(),
				videos.size(),
				dtoList
		);
	}
	
	/**
	 * Validates filter request consistency.
	 */
	private void validate(FilterRequest req) throws ResponseStatusException {
		
		if (req.getMinDurationSeconds() != null &&
					req.getMaxDurationSeconds() != null &&
					req.getMinDurationSeconds() > req.getMaxDurationSeconds()) {
			
			throw new ResponseStatusException(BAD_REQUEST, INVALID_DURATION);
		}
	}
	
	/**
	 * Converts a {@link Video} entity into a {@link VideoDto} for API response purposes.
	 *
	 * <p>This method enriches the video data with channel information by resolving the
	 * channel name from the provided channel map.</p>
	 *
	 * <p>If the channel cannot be found in the map, a fallback value of
	 * {@code "Unknown"} is used to ensure the response remains consistent
	 * and avoids null values in the API output.</p>
	 *
	 * @param v the video entity to convert
	 * @param channels a map of channelId to {@link Channel} used for enrichment
	 * @return a fully populated {@link VideoDto} ready for API response
	 */
	private VideoDto toDto(Video v, Map<String, Channel> channels) {
		Channel c = channels.get(v.getChannelId());
		
		return new VideoDto(
				v.getVideoId(),
				v.getChannelId(),
				c != null ? c.getChannelName() : UNKNOWN_CHANNEL,
				v.getTitle(),
				v.getViewCount(),
				v.getLikeCount(),
				v.getLanguage(),
				v.getDurationSeconds()
		);
	}
}