package com.example.placementSelector.service.export;

import com.example.placementSelector.model.entity.Channel;
import com.example.placementSelector.model.entity.Video;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacementCsvExporterTest {
	
	private final PlacementCsvExporter exporter = new PlacementCsvExporter();
	
	private Video video() {
		Video v = new Video();
		v.setVideoId("vid1");
		v.setChannelId("ch1");
		v.setTitle("Test Video");
		v.setViewCount(100);
		v.setLikeCount(10);
		v.setLanguage("en");
		v.setDurationSeconds(300);
		v.setPublishedAt(Instant.parse("2025-01-01T00:00:00Z"));
		return v;
	}
	
	@Test
	void shouldExportCsvWithHeaderAndRow() {
		
		Video v = video();
		
		Channel c = new Channel();
		c.setChannelId("ch1");
		c.setChannelName("Test Channel");
		
		String csv = exporter.export(
				List.of(v),
				Map.of("ch1", c)
		);
		
		assertTrue(csv.contains("videoId,channelName,title,viewCount,likeCount,language,durationSeconds"));
		assertTrue(csv.contains("vid1"));
		assertTrue(csv.contains("Test Channel"));
		assertTrue(csv.contains("Test Video"));
	}
	
	@Test
	void shouldFallbackToUnknownChannel() {
		
		Video v = video();
		
		String csv = exporter.export(
				List.of(v),
				Map.of()
		);
		
		assertTrue(csv.contains("Unknown"));
	}
}