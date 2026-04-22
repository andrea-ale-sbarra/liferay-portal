/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.client.serdes.v1_0;

import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.MostActiveVisitor;
import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.MostActiveVisitorsPage;
import com.liferay.site.dsr.analytics.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Gianmarco Brunialti
 * @generated
 */
@Generated("")
public class MostActiveVisitorsPageSerDes {

	public static MostActiveVisitorsPage toDTO(String json) {
		MostActiveVisitorsPageJSONParser mostActiveVisitorsPageJSONParser =
			new MostActiveVisitorsPageJSONParser();

		return mostActiveVisitorsPageJSONParser.parseToDTO(json);
	}

	public static MostActiveVisitorsPage[] toDTOs(String json) {
		MostActiveVisitorsPageJSONParser mostActiveVisitorsPageJSONParser =
			new MostActiveVisitorsPageJSONParser();

		return mostActiveVisitorsPageJSONParser.parseToDTOs(json);
	}

	public static String toJSON(MostActiveVisitorsPage mostActiveVisitorsPage) {
		if (mostActiveVisitorsPage == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (mostActiveVisitorsPage.getMostActiveVisitors() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"mostActiveVisitors\": ");

			sb.append("[");

			for (int i = 0;
				 i < mostActiveVisitorsPage.getMostActiveVisitors().length;
				 i++) {

				sb.append(
					String.valueOf(
						mostActiveVisitorsPage.getMostActiveVisitors()[i]));

				if ((i + 1) <
						mostActiveVisitorsPage.getMostActiveVisitors().length) {

					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (mostActiveVisitorsPage.getTotal() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"total\": ");

			sb.append(mostActiveVisitorsPage.getTotal());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		MostActiveVisitorsPageJSONParser mostActiveVisitorsPageJSONParser =
			new MostActiveVisitorsPageJSONParser();

		return mostActiveVisitorsPageJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		MostActiveVisitorsPage mostActiveVisitorsPage) {

		if (mostActiveVisitorsPage == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (mostActiveVisitorsPage.getMostActiveVisitors() == null) {
			map.put("mostActiveVisitors", null);
		}
		else {
			map.put(
				"mostActiveVisitors",
				String.valueOf(mostActiveVisitorsPage.getMostActiveVisitors()));
		}

		if (mostActiveVisitorsPage.getTotal() == null) {
			map.put("total", null);
		}
		else {
			map.put("total", String.valueOf(mostActiveVisitorsPage.getTotal()));
		}

		return map;
	}

	public static class MostActiveVisitorsPageJSONParser
		extends BaseJSONParser<MostActiveVisitorsPage> {

		@Override
		protected MostActiveVisitorsPage createDTO() {
			return new MostActiveVisitorsPage();
		}

		@Override
		protected MostActiveVisitorsPage[] createDTOArray(int size) {
			return new MostActiveVisitorsPage[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "mostActiveVisitors")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "total")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			MostActiveVisitorsPage mostActiveVisitorsPage,
			String jsonParserFieldName, Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "mostActiveVisitors")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					MostActiveVisitor[] mostActiveVisitorsArray =
						new MostActiveVisitor[jsonParserFieldValues.length];

					for (int i = 0; i < mostActiveVisitorsArray.length; i++) {
						mostActiveVisitorsArray[i] =
							MostActiveVisitorSerDes.toDTO(
								(String)jsonParserFieldValues[i]);
					}

					mostActiveVisitorsPage.setMostActiveVisitors(
						mostActiveVisitorsArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "total")) {
				if (jsonParserFieldValue != null) {
					mostActiveVisitorsPage.setTotal(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:1879718532