package com.example.placementSelector.service;

import com.example.placementSelector.model.dto.FilterRequest;
import com.example.placementSelector.model.dto.PlacementResponse;
import com.example.placementSelector.model.entity.Video;
import com.example.placementSelector.repository.DataRepository;
import com.example.placementSelector.service.export.PlacementCsvExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlacementServiceTest {
	
	@Mock
	private DataRepository repo;
	
	@Mock
	private PlacementCsvExporter csvExporter;
	
	@InjectMocks
	private PlacementService service;
	
	private Video video(long views, long likes, String lang, boolean kids, boolean embeddable) {
		Video v = new Video();
		v.setVideoId("v1");
		v.setChannelId("c1");
		v.setViewCount(views);
		v.setLikeCount(likes);
		v.setLanguage(lang);
		v.setDurationSeconds(600);
		v.setPublishedAt(Instant.parse("2025-01-01T00:00:00Z"));
		v.setIsMadeForKids(kids);
		v.setIsEmbeddable(embeddable);
		return v;
	}
	
	// View count filter
	@ParameterizedTest
	@CsvSource({
			"100, 50,  false",
			"100, 100, true",
			"100, 150, true"
	})
	void shouldFilterByMinViewCount(long minView, long videoViews, boolean expected) {
		
		Video v = video(videoViews, 0, "en", false, true);
		
		FilterRequest req = new FilterRequest();
		req.setMinViewCount(minView);
		
		when(repo.getVideos()).thenReturn(List.of(v));
		when(repo.getChannelMap()).thenReturn(Map.of());
		
		PlacementResponse response = service.filter(req);
		
		assertEquals(expected ? 1 : 0, response.getMatchedCount());
	}
	
	// Like count filter
	@ParameterizedTest
	@CsvSource({
			"100, 50,  false",
			"100, 100, true",
			"100, 150, true"
	})
	void shouldFilterByMinLikeCount(long minLikes, long videoLikes, boolean expected) {
		
		Video v = video(1000, videoLikes, "en", false, true);
		
		FilterRequest req = new FilterRequest();
		req.setMinLikeCount(minLikes);
		
		when(repo.getVideos()).thenReturn(List.of(v));
		when(repo.getChannelMap()).thenReturn(Map.of());
		
		PlacementResponse response = service.filter(req);
		
		assertEquals(expected ? 1 : 0, response.getMatchedCount());
	}
	
	// Language filter
	@ParameterizedTest
	@CsvSource({
			"en, en, true",
			"en, de, false"
	})
	void shouldFilterByLanguage(String allowed, String videoLang, boolean expected) {
		
		Video v = video(1000, 100, videoLang, false, true);
		
		FilterRequest req = new FilterRequest();
		req.setLanguages(List.of(allowed));
		
		when(repo.getVideos()).thenReturn(List.of(v));
		when(repo.getChannelMap()).thenReturn(Map.of());
		
		PlacementResponse response = service.filter(req);
		
		assertEquals(expected ? 1 : 0, response.getMatchedCount());
	}
	
	// Embeddable filter
	@ParameterizedTest
	@CsvSource({
			"true, true,  true",
			"true, false, false",
			"false, false, true"
	})
	void shouldRespectEmbeddableFilter(Boolean require, Boolean embeddable, boolean expected) {
		
		Video v = video(1000, 100, "en", false, embeddable);
		
		FilterRequest req = new FilterRequest();
		req.setRequireEmbeddable(require);
		
		when(repo.getVideos()).thenReturn(List.of(v));
		when(repo.getChannelMap()).thenReturn(Map.of());
		
		PlacementResponse response = service.filter(req);
		
		assertEquals(expected ? 1 : 0, response.getMatchedCount());
	}
	
	// Made for kids filter
	@ParameterizedTest
	@CsvSource({
			"true,  true,  false",
			"true,  false, true",
			"false, true,  true"
	})
	void shouldFilterMadeForKids(Boolean exclude, Boolean isKids, boolean expected) {
		
		Video v = video(1000, 100, "en", isKids, true);
		
		FilterRequest req = new FilterRequest();
		req.setExcludeMadeForKids(exclude);
		
		when(repo.getVideos()).thenReturn(List.of(v));
		when(repo.getChannelMap()).thenReturn(Map.of());
		
		PlacementResponse response = service.filter(req);
		
		assertEquals(expected ? 1 : 0, response.getMatchedCount());
	}
	
	// Invalid duration range
	@Test
	void shouldRejectInvalidDurationRange() {
		
		FilterRequest req = new FilterRequest();
		req.setMinDurationSeconds(500);
		req.setMaxDurationSeconds(100);
		
		assertThrows(ResponseStatusException.class,
				() -> service.filter(req));
	}
	
	// CSV Export guard
	@Test
	void shouldThrowWhenExportCalledBeforeFilter() {
		
		assertThrows(ResponseStatusException.class,
				() -> service.exportCsv());
	}
}