package com.example.placementSelector.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VideoDto {
	private String videoId;
	private String channelId;
	private String channelName;
	private String title;
	private long viewCount;
	private long likeCount;
	private String language;
	private int durationSeconds;
}
