package com.example.placementSelector.repository;

import com.example.placementSelector.model.entity.Channel;
import com.example.placementSelector.model.entity.Video;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In-memory data repository that loads video and channel data from static JSON files
 * located in the classpath at application startup.
 *
 * <p>This is a lightweight, demo-style repository that simulates a database layer
 * by keeping all data in memory after initialization.</p>
 *
 * <p>Data is loaded once during application startup using {@link PostConstruct},
 * and parsed using a Spring-managed {@link ObjectMapper}.</p>
 */
@Getter
@Repository
@RequiredArgsConstructor
public class DataRepository {
	
	private List<Video> videos;
	private Map<String, Channel> channelMap;
	
	private final ObjectMapper mapper;
	
	/**
	 * Loads video and channel data from JSON files into in-memory structures.
	 *
	 * <p>This method is executed once during application startup.</p>
	 *
	 * @throws IOException if JSON files cannot be read from the classpath
	 */
	@PostConstruct
	public void load() throws IOException {

		Video[] videoArray = loadJson("videos.json", Video[].class);
		videos = List.of(videoArray);
		
		Channel[] channels = loadJson("channels.json", Channel[].class);
		
		channelMap = Arrays.stream(channels)
							.collect(Collectors.toMap(
									Channel::getChannelId,
									c -> c,
									(existing, replacement) -> existing
							));
	}
	
	private <T> T loadJson(String path, Class<T> type) throws IOException {
		return mapper.readValue(
				new ClassPathResource(path).getInputStream(),
				type
		);
	}
}
