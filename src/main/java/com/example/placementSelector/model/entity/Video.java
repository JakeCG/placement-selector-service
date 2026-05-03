package com.example.placementSelector.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class Video {
	private String videoId;
	private String channelId;
	private String title;
	private long viewCount;
	private long likeCount;
	private int categoryId;
	private String language;
	private Instant publishedAt;
	private int durationSeconds;
	@JsonProperty("isMadeForKids")
	private Boolean isMadeForKids;
	@JsonProperty("isEmbeddable")
	private Boolean isEmbeddable;
}
