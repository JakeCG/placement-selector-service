package com.example.placementSelector.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

class CsvUtilTest {
	
	@Test
	void shouldReturnEmptyString_whenNull() {
		assertEquals("", CsvUtil.escape(null));
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	void shouldReturnEmptyString_whenNullOrEmpty(String input) {
		assertEquals("", CsvUtil.escape(input));
	}
	
	@ParameterizedTest
	@MethodSource("unescapedValues")
	void shouldNotEscapePlainValues(String input) {
		assertEquals(input, CsvUtil.escape(input));
	}
	
	@ParameterizedTest
	@MethodSource("escapedValues")
	void shouldEscapeValues(String input, String expected) {
		assertEquals(expected, CsvUtil.escape(input));
	}
	
	static Stream<String> unescapedValues() {
		return Stream.of(
				"hello",
				"12345",
				"simpleText"
		);
	}
	
	static Stream<org.junit.jupiter.params.provider.Arguments> escapedValues() {
		return Stream.of(
				of(
						"a,b",
						"\"a,b\""
				),
				of(
						"a\"b",
						"\"a\"\"b\""
				),
				of(
						"a\nb",
						"\"a\nb\""
				),
				of(
						"he said \"hi\"",
						"\"he said \"\"hi\"\"\""
				)
		);
	}
}