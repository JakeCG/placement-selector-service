package com.example.placementSelector.util;

/**
 * Utility class for escaping values for CSV output.
 *
 * <p>Applies basic CSV escaping rules:
 * <ul>
 *     <li>Wraps values in double quotes if they contain commas, quotes, or new lines</li>
 *     <li>Escapes double quotes by doubling them (" → "")</li>
 *     <li>Returns empty string for null values</li>
 * </ul>
 */
public class CsvUtil {
	
	public static String escape(String value) {
		if (value == null) return "";
		
		boolean needsEscaping = value.contains(",") || value.contains("\"") || value.contains("\n");
		
		if (needsEscaping) {
			value = value.replace("\"", "\"\"");
			return "\"" + value + "\"";
		}
		
		return value;
	}
}
