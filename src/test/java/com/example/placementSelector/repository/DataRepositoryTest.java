package com.example.placementSelector.repository;

import com.example.placementSelector.model.entity.Channel;
import com.example.placementSelector.model.entity.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DataRepositoryTest {
	
	@Autowired
	private DataRepository repository;
	
	@Test
	void shouldLoadVideosOnStartup() {
		assertNotNull(repository.getVideos());
		assertFalse(repository.getVideos().isEmpty());
	}
	
	@Test
	void shouldLoadChannelMapOnStartup() {
		assertNotNull(repository.getChannelMap());
		assertFalse(repository.getChannelMap().isEmpty());
	}
	
	@Test
	void shouldContainExpectedChannel() {
		Channel channel = repository.getChannelMap().get("UC38IQsAvIsxxjztdMZQtwHA");
		
		assertNotNull(channel);
		assertEquals("Bread Lab", channel.getChannelName());
	}
	
	@Test
	void shouldContainVideosLinkedToChannels() {
		Video video = repository.getVideos().stream()
							  .filter(v -> v.getVideoId().equals("lBDdz1C5Jau"))
							  .findFirst()
							  .orElseThrow();
		
		assertEquals("UC38IQsAvIsxxjztdMZQtwHA", video.getChannelId());
	}
}