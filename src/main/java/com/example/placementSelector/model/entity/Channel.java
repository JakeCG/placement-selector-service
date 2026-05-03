package com.example.placementSelector.model.entity;

import lombok.Data;

@Data
public class Channel {
	private String channelId;
	private String channelName;
	private long subscriberCount;
	private String country;
}
