package com.example.placementSelector.service.export;

import com.example.placementSelector.model.entity.Channel;
import com.example.placementSelector.model.entity.Video;
import com.example.placementSelector.util.CsvUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PlacementCsvExporter {
	
	private static final String CSV_HEADER =
			"videoId,channelName,title,viewCount,likeCount,language,durationSeconds\n";
	
	private static final String UNKNOWN_CHANNEL = "Unknown";
	private static final String COMMA = ",";
	
	/**
	 * Converts a list of videos into a CSV string.
	 *
	 * <p>This method formats video placement data using the provided channel map
	 * to enrich each video with its corresponding channel name.</p>
	 *
	 * @param videos filtered video placements
	 * @param channels map of channelId to Channel metadata
	 * @return CSV formatted string
	 */
	public String export(List<Video> videos, Map<String, Channel> channels) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(CSV_HEADER);
		
		for (Video v : videos) {
			appendRow(v, channels, sb);
		}
		
		return sb.toString();
	}
	
	/**
	 * Appends a single video as a CSV row to the provided StringBuilder.
	 *
	 * <p>Resolves channel information from the provided channel map and applies
	 * fallback handling for missing channels.</p>
	 */
	private void appendRow(Video v, Map<String, Channel> channels, StringBuilder sb) {
		
		Channel c = channels.get(v.getChannelId());
		String channelName = (c != null) ? c.getChannelName() : UNKNOWN_CHANNEL;
		
		sb.append(CsvUtil.escape(v.getVideoId())).append(COMMA);
		sb.append(CsvUtil.escape(channelName)).append(COMMA);
		sb.append(CsvUtil.escape(v.getTitle())).append(COMMA);
		sb.append(v.getViewCount()).append(COMMA);
		sb.append(v.getLikeCount()).append(COMMA);
		sb.append(v.getLanguage()).append(COMMA);
		sb.append(v.getDurationSeconds()).append("\n");
	}
}