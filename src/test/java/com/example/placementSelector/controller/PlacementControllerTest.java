package com.example.placementSelector.controller;

import com.example.placementSelector.model.dto.FilterRequest;
import com.example.placementSelector.model.dto.PlacementResponse;
import com.example.placementSelector.service.PlacementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlacementController.class)
class PlacementControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private PlacementService service;
	
	// POST /filter
	@Test
	void shouldFilterVideos() throws Exception {
		
		FilterRequest req = new FilterRequest();
		
		PlacementResponse response = new PlacementResponse(10, 5, List.of());
		
		when(service.filter(any())).thenReturn(response);
		
		mockMvc.perform(post("/api/placements/filter")
								.contentType(APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk());
	}
	
	// GET /export (valid)
	@Test
	void shouldExportCsv() throws Exception {
		
		when(service.exportCsv()).thenReturn("csv-data");
		
		mockMvc.perform(get("/api/placements/export")
								.param("format", "csv"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Content-Disposition"))
				.andExpect(content().string("csv-data"));
	}
	
	// GET /export (invalid)
	@Test
	void shouldRejectInvalidExportFormat() throws Exception {
		
		mockMvc.perform(get("/api/placements/export")
								.param("format", "json"))
				.andExpect(status().isBadRequest());
	}
	
	// DELETE /filter
	@Test
	void shouldResetState() throws Exception {
		
		mockMvc.perform(delete("/api/placements/filter"))
				.andExpect(status().isNoContent());
		
		verify(service, times(1)).reset();
	}
}